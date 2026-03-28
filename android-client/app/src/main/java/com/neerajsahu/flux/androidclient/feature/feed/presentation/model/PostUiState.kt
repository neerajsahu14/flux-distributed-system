package com.neerajsahu.flux.androidclient.feature.feed.presentation.model

import com.neerajsahu.flux.androidclient.feature.feed.domain.model.MediaType

data class AttachmentUiState(
    val id: Long,
    val type: MediaType,
    val thumbnailUrl: String?,
    val contentUrl: String,
    val displayOrder: Int
)

data class PostUiState(
    val id: Long,
    val authorId: Long,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val timestamp: String,
    val caption: String?,
    val mainMediaType: MediaType,
    val mainThumbnailUrl: String,
    val mainContentUrl: String?,
    val likeCount: Int,
    val shareCount: Int,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val attachments: List<AttachmentUiState>
)
