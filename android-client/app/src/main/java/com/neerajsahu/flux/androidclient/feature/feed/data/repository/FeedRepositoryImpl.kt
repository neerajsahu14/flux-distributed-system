package com.neerajsahu.flux.androidclient.feature.feed.data.repository

import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.feed.data.local.PostDao
import com.neerajsahu.flux.androidclient.feature.feed.data.remote.FeedApi
import com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto.PostResponseDto
import com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto.UpdatePostRequestDto
import com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto.toDomain
import com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto.toEntity
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.Post
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.PostDetail
import com.neerajsahu.flux.androidclient.feature.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val feedApi: FeedApi,
    private val postDao: PostDao
) : FeedRepository {

    override fun getGlobalFeed(page: Int, size: Int): Flow<AppResult<List<Post>>> {
        return loadFeedScope(scope = "global") { feedApi.getGlobalFeed(page, size) }
    }

    override fun getTimelineFeed(page: Int, size: Int): Flow<AppResult<List<Post>>> {
        return loadFeedScope(scope = "timeline") { feedApi.getTimelineFeed(page, size) }
    }

    override fun getUserFeed(userId: Long, page: Int, size: Int): Flow<AppResult<List<Post>>> {
        if (userId <= 0L) {
            return flow { emit(AppResult.Error("Invalid user id")) }
        }
        return loadFeedScope(scope = "user:$userId") { feedApi.getUserFeed(userId, page, size) }
    }

    override suspend fun getPostDetail(postId: Long): AppResult<PostDetail> {
        return try {
            AppResult.Success(feedApi.getPostDetail(postId).toDomain())
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun createPost(
        image: MultipartBody.Part,
        caption: String?,
        requestId: String
    ): AppResult<Post> {
        return try {
            val captionBody = caption?.toRequestBody("text/plain".toMediaType())
            val requestIdBody = requestId.toRequestBody("text/plain".toMediaType())
            val createdPost = feedApi.createPost(image, captionBody, requestIdBody).toDomain()
            postDao.clearAll()
            AppResult.Success(createdPost)
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun updatePost(postId: Long, caption: String?): AppResult<Post> {
        return try {
            val updatedPost = feedApi.updatePost(postId, UpdatePostRequestDto(caption)).toDomain()
            postDao.clearAll()
            AppResult.Success(updatedPost)
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage())
        }
    }

    override suspend fun deletePost(postId: Long): AppResult<Unit> {
        return try {
            feedApi.deletePost(postId)
            postDao.clearAll()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.toReadableMessage())
        }
    }

    private fun loadFeedScope(
        scope: String,
        networkCall: suspend () -> List<PostResponseDto>
    ): Flow<AppResult<List<Post>>> = flow {
        val cachedPosts = postDao.getPostsByScope(scope)
        if (cachedPosts.isNotEmpty()) {
            emit(AppResult.Success(cachedPosts.map { it.toDomain() }))
        }

        try {
            val remotePosts = networkCall()
            postDao.replaceScope(scope, remotePosts.map { it.toEntity(scope) })
            emit(AppResult.Success(remotePosts.map { it.toDomain() }))
        } catch (e: Exception) {
            if (cachedPosts.isEmpty()) {
                emit(AppResult.Error(e.toReadableMessage()))
            }
        }
    }

    private fun Exception.toReadableMessage(): String {
        return when (this) {
            is HttpException -> response()?.errorBody()?.string() ?: "Server error"
            is IOException -> "No internet connection"
            else -> message ?: "Unknown error occurred"
        }
    }
}

