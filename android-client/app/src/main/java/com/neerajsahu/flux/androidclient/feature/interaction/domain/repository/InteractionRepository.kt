package com.neerajsahu.flux.androidclient.feature.interaction.domain.repository

import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.Post

data class InteractionActionResult(
    val postId: Long,
    val actionType: String,
    val message: String,
    val currentCount: Long? = null
)

interface InteractionRepository {
    suspend fun likePost(postId: Long): AppResult<InteractionActionResult>
    suspend fun unlikePost(postId: Long): AppResult<InteractionActionResult>
    suspend fun bookmarkPost(postId: Long): AppResult<InteractionActionResult>
    suspend fun unbookmarkPost(postId: Long): AppResult<InteractionActionResult>
    suspend fun sharePost(postId: Long): AppResult<InteractionActionResult>

    suspend fun getBookmarkedPosts(page: Int = 0, size: Int = 20): AppResult<List<Post>>
    suspend fun getLikedPosts(page: Int = 0, size: Int = 20): AppResult<List<Post>>

    suspend fun isPostLiked(postId: Long): AppResult<Boolean>
    suspend fun isPostBookmarked(postId: Long): AppResult<Boolean>
}

