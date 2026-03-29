package com.neerajsahu.flux.androidclient.feature.relationship.data.repository

import android.content.Context
import androidx.work.*
import com.neerajsahu.flux.androidclient.core.datastore.TokenManager
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.core.utils.ErrorParser
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.PendingActionEntity
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.ProfileStatsDao
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.RelationshipApi
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto.*
import com.neerajsahu.flux.androidclient.feature.relationship.data.worker.FollowWorker
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.ProfileStats
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.RelationshipUser
import com.neerajsahu.flux.androidclient.feature.relationship.domain.repository.RelationshipRepository
import com.neerajsahu.flux.androidclient.feature.relationship.mapper.*
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RelationshipRepositoryImpl @Inject constructor(
    private val api: RelationshipApi,
    private val profileStatsDao: ProfileStatsDao,
    private val errorParser: ErrorParser,
    private val context: Context,
    private val tokenManager: TokenManager
) : RelationshipRepository {

    private val CACHE_LIMIT_STATS = 20
    private val CACHE_LIMIT_PROFILES = 50

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

    override fun getFollowers(userId: Long, page: Int, size: Int): Flow<AppResult<List<RelationshipUser>>> = flow {
        try {
            val response = api.getFollowers(userId, page, size)
            val entities = response.map { it.toProfileEntity() }
            profileStatsDao.insertProfiles(entities)
            
            val currentUserId = tokenManager.getUserId().first() ?: 0L
            profileStatsDao.pruneOldProfiles(currentUserId, CACHE_LIMIT_PROFILES)
            
            emit(AppResult.Success(response.map { it.toRelationshipUser() }))
        } catch (e: Exception) {
            emit(AppResult.Error(e.message ?: "Failed to load followers"))
        }
    }

    override fun getFollowing(userId: Long, page: Int, size: Int): Flow<AppResult<List<RelationshipUser>>> = flow {
        try {
            val response = api.getFollowing(userId, page, size)
            val entities = response.map { it.toProfileEntity() }
            profileStatsDao.insertProfiles(entities)
            
            val currentUserId = tokenManager.getUserId().first() ?: 0L
            profileStatsDao.pruneOldProfiles(currentUserId, CACHE_LIMIT_PROFILES)
            
            emit(AppResult.Success(response.map { it.toRelationshipUser() }))
        } catch (e: Exception) {
            emit(AppResult.Error(e.message ?: "Failed to load following"))
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

            profileStatsDao.pruneOldProfileStats(tokenUserId, CACHE_LIMIT_STATS)

            emit(AppResult.Success(remoteStats.toProfileStats()))
        } catch (e: HttpException) {
            if (cached == null) {
                emit(AppResult.Error(errorParser.parse(e.response()?.errorBody()?.string())))
            }
        } catch (e: IOException) {
            if (cached == null) {
                emit(AppResult.Error("No internet connection"))
            }
        }
    }

    override fun getCurrentUserProfileStats(): Flow<AppResult<ProfileStats>> = flow {
        val currentUserId = tokenManager.getUserId().first() ?: 0L
        
        var cachedStats: ProfileStats? = null
        if (currentUserId > 0L) {
            val cached = profileStatsDao.getProfileStatsById(currentUserId).first()
            if (cached != null) {
                cachedStats = cached.toProfileStats()
                emit(AppResult.Success(cachedStats))
            }
        }

        try {
            val response = api.getCurrentUserProfileStats()
            profileStatsDao.insertProfileStats(response.toProfileStatsEntity())
            
            val userId = response.profile.id
            if (currentUserId == 0L) {
                tokenManager.saveUserId(userId)
            }
            
            emit(AppResult.Success(response.toProfileStats()))
        } catch (e: Exception) {
            if (cachedStats == null) {
                emit(AppResult.Error(e.message ?: "Failed to load profile stats"))
            }
        }
    }

    override fun searchUsers(query: String, page: Int, size: Int): Flow<AppResult<List<RelationshipUser>>> = flow {
        // Emit cached results matching the query first
        val cached = profileStatsDao.searchProfiles(query, size).first()
        if (cached.isNotEmpty()) {
            emit(AppResult.Success(cached.map { it.toRelationshipUser() }))
        }

        try {
            val response = api.searchUsers(query, page, size)
            val entities = response.map { it.toProfileEntity() }
            profileStatsDao.insertProfiles(entities)
            
            val currentUserId = tokenManager.getUserId().first() ?: 0L
            profileStatsDao.pruneOldProfiles(currentUserId, CACHE_LIMIT_PROFILES)
            
            emit(AppResult.Success(response.map { it.toRelationshipUser() }))
        } catch (e: Exception) {
            if (cached.isEmpty()) {
                emit(AppResult.Error(e.message ?: "Search failed"))
            }
        }
    }
}
