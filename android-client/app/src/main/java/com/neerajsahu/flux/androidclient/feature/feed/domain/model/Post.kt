package com.neerajsahu.flux.androidclient.feature.feed.domain.model

data class FeedAuthor(
    val id: Long,
    val username: String,
    val profilePicUrl: String?
)

data class Post(
    val id: Long,
    val caption: String?,
    val imageUrl: String,
    val author: FeedAuthor,
    val createdAt: String,
    val likeCount: Int,
    val shareCount: Int = 0,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false
)

data class Attachment(
    val id: Long,
    val contentUrl: String,
    val thumbnailUrl: String?,
    val mediaType: String,
    val displayOrder: Int
)

data class PostDetail(
    val id: Long,
    val caption: String?,
    val imageUrl: String,
    val contentUrl: String?,
    val mediaType: String,
    val author: FeedAuthor,
    val createdAt: String,
    val likeCount: Int,
    val shareCount: Int,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val attachments: List<Attachment>
)

