package com.neerajsahu.flux.androidclient.feature.relationship.data.repository

import android.content.Context
import androidx.work.*
import com.neerajsahu.flux.androidclient.core.datastore.TokenManager
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.PendingActionEntity
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.ProfileStatsDao
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.RelationshipApi
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto.*
import com.neerajsahu.flux.androidclient.feature.relationship.data.worker.FollowWorker
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.ProfileStats
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.RelationshipUser
import com.neerajsahu.flux.androidclient.feature.relationship.domain.repository.RelationshipRepository
import com.neerajsahu.flux.androidclient.feature.relationship.mapper.toProfileStats
import com.neerajsahu.flux.androidclient.feature.relationship.mapper.toProfileStatsEntity
import com.neerajsahu.flux.androidclient.feature.relationship.mapper.toRelationshipUser
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RelationshipRepositoryImpl @Inject constructor(
    private val api: RelationshipApi,
    private val profileStatsDao: ProfileStatsDao,
    private val context: Context,
    private val tokenManager: TokenManager
) : RelationshipRepository {

    private val CACHE_LIMIT = 20

    override suspend fun toggleFollow(targetUserId: Long, requestId: String): AppResult<FollowActionResponse> {
        return try {
            val response = api.toggleFollow(targetUserId, FollowRequest(requestId))
            AppResult.Success(response)
        } catch (e: Exception) {
            if (e is IOException || (e is HttpException && e.code() >= 500)) {
                val actionType = "TOGGLE"
                profileStatsDao.insertPendingAction(
                    PendingActionEntity(
                        targetUserId = targetUserId,
                        actionType = actionType,
                        requestId = requestId
                    )
                )
                scheduleFollowWorker()
                AppResult.Error("Offline: Action will sync when online")
            } else {
                AppResult.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    private fun scheduleFollowWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<FollowWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "follow_sync_work",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            workRequest
        )
    }

    override suspend fun getFollowers(userId: Long, page: Int, size: Int): AppResult<List<RelationshipUser>> {
        return try {
            val response = api.getFollowers(userId, page, size)
            AppResult.Success(response.map { it.toRelationshipUser() })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getFollowing(userId: Long, page: Int, size: Int): AppResult<List<RelationshipUser>> {
        return try {
            val response = api.getFollowing(userId, page, size)
            AppResult.Success(response.map { it.toRelationshipUser() })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getRelationshipInfo(targetUserId: Long): AppResult<RelationshipInfoResponse> {
        return try {
            val response = api.getRelationshipInfo(targetUserId)
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    override fun getProfileStats(userId: Long): Flow<AppResult<ProfileStats>> = flow {
        val tokenUserId = tokenManager.getUserId().first() ?: 0L
        val resolvedUserId = if (userId == 0L) tokenUserId else userId

        if (resolvedUserId <= 0L) {
            emit(AppResult.Error("Invalid user id"))
            return@flow
        }

        val cached = profileStatsDao.getProfileStatsById(resolvedUserId).first()
        if (cached != null) {
            profileStatsDao.updateLastAccessed(resolvedUserId)
            emit(AppResult.Success(cached.toProfileStats()))
        }

        try {
            val remoteStats = api.getTargetProfileStats(resolvedUserId)
            profileStatsDao.insertProfileStats(remoteStats.toProfileStatsEntity())

            val currentUserId = if (tokenUserId > 0L) tokenUserId else resolvedUserId
            profileStatsDao.pruneOldProfiles(currentUserId, CACHE_LIMIT)

            emit(AppResult.Success(remoteStats.toProfileStats()))
        } catch (e: HttpException) {
            if (cached == null) {
                emit(AppResult.Error(e.response()?.errorBody()?.string() ?: "Unknown error"))
            }
        } catch (e: IOException) {
            if (cached == null) {
                emit(AppResult.Error("No internet connection"))
            }
        }
    }

    override suspend fun getCurrentUserProfileStats(): AppResult<ProfileStatsResponse> {
        return try {
            val response = api.getCurrentUserProfileStats()
            profileStatsDao.insertProfileStats(response.toProfileStatsEntity())
            AppResult.Success(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun searchUsers(query: String, page: Int, size: Int): AppResult<List<RelationshipUser>> {
        return try {
            val response = api.searchUsers(query, page, size)
            AppResult.Success(response.map { it.toRelationshipUser() })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Search failed")
        }
    }
}
