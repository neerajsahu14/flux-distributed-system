package com.neerajsahu.flux.androidclient.feature.interaction.domain.model

data class PostInteractionState(
    val postId: Long,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val likeCount: Int,
    val shareCount: Int
)

