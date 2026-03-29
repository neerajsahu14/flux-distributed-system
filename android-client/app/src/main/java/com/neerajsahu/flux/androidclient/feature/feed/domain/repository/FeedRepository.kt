package com.neerajsahu.flux.androidclient.feature.feed.domain.repository

import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.Post
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.PostDetail
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody

interface FeedRepository {
    fun getGlobalFeed(page: Int = 0, size: Int = 20, forceRefresh: Boolean = false): Flow<AppResult<List<Post>>>
    fun getTimelineFeed(page: Int = 0, size: Int = 20, forceRefresh: Boolean = false): Flow<AppResult<List<Post>>>
    fun getUserFeed(userId: Long, page: Int = 0, size: Int = 20, forceRefresh: Boolean = false): Flow<AppResult<List<Post>>>

    suspend fun getPostDetail(postId: Long): AppResult<PostDetail>
    suspend fun createPost(
        image: MultipartBody.Part,
        caption: String?,
        requestId: String
    ): AppResult<Post>

    suspend fun updatePost(postId: Long, caption: String?): AppResult<Post>
    suspend fun deletePost(postId: Long): AppResult<Unit>
}
