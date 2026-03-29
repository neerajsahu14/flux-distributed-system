package com.neerajsahu.flux.androidclient.feature.interaction.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.neerajsahu.flux.androidclient.feature.interaction.data.remote.InteractionApi
import com.neerajsahu.flux.androidclient.feature.interaction.data.remote.dto.InteractionRequestDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID

@HiltWorker
class InteractionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val interactionApi: InteractionApi
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val postId = inputData.getLong("POST_ID", -1L)
        val actionType = inputData.getString("ACTION_TYPE")

        if (postId == -1L || actionType.isNullOrBlank()) {
            return Result.failure()
        }

        return try {
            val requestDto = InteractionRequestDto(UUID.randomUUID().toString())
            when (actionType) {
                "LIKE" -> interactionApi.likePost(postId, requestDto)
                "UNLIKE" -> interactionApi.unlikePost(postId, requestDto)
                "BOOKMARK" -> interactionApi.bookmarkPost(postId, requestDto)
                "UNBOOKMARK" -> interactionApi.unbookmarkPost(postId, requestDto)
                "SHARE" -> interactionApi.sharePost(postId, requestDto)
                else -> return Result.failure()
            }
            Result.success()
        } catch (e: Exception) {
            // Re-schedule for later if network fails
            Result.retry()
        }
    }
}

