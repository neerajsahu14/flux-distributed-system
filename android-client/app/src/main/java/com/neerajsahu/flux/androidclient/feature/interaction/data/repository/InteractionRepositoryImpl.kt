package com.neerajsahu.flux.androidclient.feature.interaction.data.repository

import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.core.utils.ErrorParser
import com.neerajsahu.flux.androidclient.feature.feed.data.local.PostDao
import com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto.toDomain
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.Post
import com.neerajsahu.flux.androidclient.feature.interaction.data.local.InteractionDao
import com.neerajsahu.flux.androidclient.feature.interaction.data.remote.InteractionApi
import com.neerajsahu.flux.androidclient.feature.interaction.data.remote.dto.InteractionRequestDto
import com.neerajsahu.flux.androidclient.feature.interaction.data.remote.dto.toDomain
import com.neerajsahu.flux.androidclient.feature.interaction.domain.repository.InteractionActionResult
import com.neerajsahu.flux.androidclient.feature.interaction.domain.repository.InteractionRepository
import com.neerajsahu.flux.androidclient.feature.interaction.workers.InteractionWorker
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import androidx.work.BackoffPolicy
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InteractionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val interactionApi: InteractionApi,
    private val interactionDao: InteractionDao,
    private val postDao: PostDao,
    private val errorParser: ErrorParser
) : InteractionRepository {

    private fun enqueueInteractionWork(postId: Long, actionType: String): AppResult<InteractionActionResult> {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<InteractionWorker>()
            .setInputData(workDataOf("POST_ID" to postId, "ACTION_TYPE" to actionType))
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)

        // Optimistic success, always return success for the UI right away
        return AppResult.Success(InteractionActionResult(postId, actionType, "Action queued offline"))
    }

    override suspend fun likePost(postId: Long): AppResult<InteractionActionResult> {
        return enqueueInteractionWork(postId, "LIKE")
    }

    override suspend fun unlikePost(postId: Long): AppResult<InteractionActionResult> {
        return enqueueInteractionWork(postId, "UNLIKE")
    }

    override suspend fun bookmarkPost(postId: Long): AppResult<InteractionActionResult> {
        return enqueueInteractionWork(postId, "BOOKMARK")
    }

    override suspend fun unbookmarkPost(postId: Long): AppResult<InteractionActionResult> {
        return enqueueInteractionWork(postId, "UNBOOKMARK")
    }

    override suspend fun sharePost(postId: Long): AppResult<InteractionActionResult> {
        return enqueueInteractionWork(postId, "SHARE")
    }

    override suspend fun getBookmarkedPosts(page: Int, size: Int): AppResult<List<Post>> {
        return try {
            val posts = interactionApi.getBookmarkedPosts(page, size).map { it.toDomain() }
            AppResult.Success(mergeInteractionState(posts))
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun getLikedPosts(page: Int, size: Int): AppResult<List<Post>> {
        return try {
            val posts = interactionApi.getLikedPosts(page, size).map { it.toDomain() }
            AppResult.Success(mergeInteractionState(posts))
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun isPostLiked(postId: Long): AppResult<Boolean> {
        return try {
            val liked = interactionApi.isPostLiked(postId)["liked"] ?: false
            interactionDao.updateLiked(postId, liked)
            postDao.updateLikeState(postId, liked, 0)
            AppResult.Success(liked)
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun isPostBookmarked(postId: Long): AppResult<Boolean> {
        return try {
            val bookmarked = interactionApi.isPostBookmarked(postId)["bookmarked"] ?: false
            interactionDao.updateBookmarked(postId, bookmarked)
            postDao.updateBookmarkState(postId, bookmarked)
            AppResult.Success(bookmarked)
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage())
        }
    }

    private suspend fun mergeInteractionState(posts: List<Post>): List<Post> {
        if (posts.isEmpty()) return posts
        val interactionByPostId = interactionDao
            .getByPostIds(posts.map { it.id })
            .associateBy { it.postId }

        return posts.map { post ->
            val localState = interactionByPostId[post.id]
            post.copy(
                isLiked = localState?.isLiked ?: post.isLiked,
                isBookmarked = localState?.isBookmarked ?: post.isBookmarked
            )
        }
    }

    private suspend fun request(
        call: suspend () -> com.neerajsahu.flux.androidclient.feature.interaction.data.remote.dto.InteractionResponseDto
    ): AppResult<InteractionActionResult> {
        return try {
            AppResult.Success(call().toDomain())
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage())
        }
    }

    private fun Exception.toReadableMessage(): String {
        return when (this) {
            is HttpException -> errorParser.parse(response()?.errorBody()?.string())
            is IOException -> "No internet connection"
            else -> message ?: "Unknown error occurred"
        }
    }
}
