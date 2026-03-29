package com.neerajsahu.flux.androidclient.feature.feed.data.repository

import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.core.utils.ErrorParser
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
    private val postDao: PostDao,
    private val errorParser: ErrorParser
) : FeedRepository {

    override fun getGlobalFeed(page: Int, size: Int, forceRefresh: Boolean): Flow<AppResult<List<Post>>> {
        return loadFeedScope(scope = "global", page = page, forceRefresh = forceRefresh) { feedApi.getGlobalFeed(page, size) }
    }

    override fun getTimelineFeed(page: Int, size: Int, forceRefresh: Boolean): Flow<AppResult<List<Post>>> {
        return loadFeedScope(scope = "timeline", page = page, forceRefresh = forceRefresh) { feedApi.getTimelineFeed(page, size) }
    }

    override fun getUserFeed(userId: Long, page: Int, size: Int, forceRefresh: Boolean): Flow<AppResult<List<Post>>> {
        if (userId <= 0L) {
            return flow { emit(AppResult.Error("Invalid user id")) }
        }
        return loadFeedScope(scope = "user:$userId", page = page, forceRefresh = forceRefresh) { feedApi.getUserFeed(userId, page, size) }
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
        page: Int,
        forceRefresh: Boolean,
        networkCall: suspend () -> List<PostResponseDto>
    ): Flow<AppResult<List<Post>>> = flow {
        val cachedPosts = postDao.getPostsByScope(scope)
        
        // TTL setup: 5 minutes (300,000 milliseconds)
        val TTL_MILLIS = 5 * 60 * 1000L
        val isCacheExpired = cachedPosts.isEmpty() || (System.currentTimeMillis() - cachedPosts.first().cachedAt > TTL_MILLIS)

        if (cachedPosts.isNotEmpty() && (!forceRefresh && !isCacheExpired) && page == 0) {
            emit(AppResult.Success(cachedPosts.map { it.toDomain() }))
            return@flow
        }
        
        // if we are here and we have cache (but it's expired) emit it first for immediate UI
        if (cachedPosts.isNotEmpty() && page == 0) {
            emit(AppResult.Success(cachedPosts.map { it.toDomain() }))
        }

        try {
            val remotePosts = networkCall()
            if (forceRefresh || page == 0) {
                postDao.replaceScope(scope, remotePosts.map { it.toEntity(scope) })
                val updatedCache = postDao.getPostsByScope(scope)
                emit(AppResult.Success(updatedCache.map { it.toDomain() }))
            } else {
                postDao.upsertPosts(remotePosts.map { it.toEntity(scope) })
                emit(AppResult.Success(remotePosts.map { it.toDomain() }))
            }
        } catch (e: Exception) {
            if (cachedPosts.isEmpty() || page > 0) {
                emit(AppResult.Error(e.toReadableMessage()))
            } else {
                emit(AppResult.Success(cachedPosts.map { it.toDomain() }))
            }
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
