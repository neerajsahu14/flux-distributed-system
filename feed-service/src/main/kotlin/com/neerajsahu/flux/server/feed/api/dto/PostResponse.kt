package com.neerajsahu.flux.server.feed.api.dto

import com.neerajsahu.flux.server.auth.api.dto.UserResponse

data class PostResponse(
    val id: Long,
    val caption: String?,
    val imageUrl: String,
    val author: UserResponse,

    val createdAt: String,
    val likeCount: Int,
)