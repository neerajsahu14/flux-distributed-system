package com.neerajsahu.flux.server.feed.api.dto

import com.neerajsahu.flux.server.auth.api.dto.UserResponse

data class CreatePostRequest(
    val caption: String?,
    val requestId: String
)

data class PostDetailResponse(
    val id: Long,
    val caption: String?,
    val imageUrl: String,
    val contentUrl: String?,  // Original high-res or video URL
    val mediaType: String,    // IMAGE or VIDEO
    val author: UserResponse,
    val createdAt: String,
    val likeCount: Int,
    val shareCount: Int,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val attachments: List<AttachmentResponse>
)

data class AttachmentResponse(
    val id: Long,
    val contentUrl: String,
    val thumbnailUrl: String?,
    val mediaType: String,
    val displayOrder: Int
)