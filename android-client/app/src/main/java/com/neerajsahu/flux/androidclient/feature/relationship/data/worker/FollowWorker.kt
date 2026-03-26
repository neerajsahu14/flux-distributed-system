package com.neerajsahu.flux.androidclient.feature.relationship.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.ProfileStatsDao
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.RelationshipApi
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto.FollowRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException

@HiltWorker
class FollowWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: RelationshipApi,
    private val dao: ProfileStatsDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pendingActions = dao.getAllPendingActions()
        if (pendingActions.isEmpty()) return Result.success()

        var allSuccess = true

        for (action in pendingActions) {
            try {
                // Since the backend toggleFollow is idempotent with requestId, 
                // we just call it. If it was already followed, it won't change anything 
                // but will return success.
                api.toggleFollow(action.targetUserId, FollowRequest(action.requestId))
                dao.deletePendingAction(action)
            } catch (e: HttpException) {
                if (e.code() in 400..499) {
                    // Client error, probably invalid request, delete it to avoid infinite retries
                    dao.deletePendingAction(action)
                } else {
                    allSuccess = false
                }
            } catch (e: Exception) {
                allSuccess = false
            }
        }

        return if (allSuccess) Result.success() else Result.retry()
    }
}
