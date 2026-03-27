package com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.neerajsahu.flux.androidclient.feature.feed.data.local.PostEntity
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.Attachment
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.FeedAuthor
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.Post
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.PostDetail

data class FeedAuthorDto(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("profilePicUrl") val profilePicUrl: String?
)

data class PostResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("caption") val caption: String?,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("author") val author: FeedAuthorDto,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("likeCount") val likeCount: Int
)

data class AttachmentResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("contentUrl") val contentUrl: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName("mediaType") val mediaType: String,
    @SerializedName("displayOrder") val displayOrder: Int
)

data class PostDetailResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("caption") val caption: String?,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("contentUrl") val contentUrl: String?,
    @SerializedName("mediaType") val mediaType: String,
    @SerializedName("author") val author: FeedAuthorDto,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("likeCount") val likeCount: Int,
    @SerializedName("shareCount") val shareCount: Int,
    @SerializedName("isLiked") val isLiked: Boolean,
    @SerializedName("isBookmarked") val isBookmarked: Boolean,
    @SerializedName("attachments") val attachments: List<AttachmentResponseDto>
)

data class UpdatePostRequestDto(
    @SerializedName("caption") val caption: String?
)

fun PostResponseDto.toEntity(scope: String): PostEntity {
    return PostEntity(
        id = id,
        scope = scope,
        caption = caption,
        imageUrl = imageUrl,
        authorId = author.id,
        authorUsername = author.username,
        authorProfilePicUrl = author.profilePicUrl,
        createdAt = createdAt,
        likeCount = likeCount
    )
}

fun PostEntity.toDomain(): Post {
    return Post(
        id = id,
        caption = caption,
        imageUrl = imageUrl,
        author = FeedAuthor(
            id = authorId,
            username = authorUsername,
            profilePicUrl = authorProfilePicUrl
        ),
        createdAt = createdAt,
        likeCount = likeCount
    )
}

fun PostResponseDto.toDomain(): Post {
    return Post(
        id = id,
        caption = caption,
        imageUrl = imageUrl,
        author = FeedAuthor(
            id = author.id,
            username = author.username,
            profilePicUrl = author.profilePicUrl
        ),
        createdAt = createdAt,
        likeCount = likeCount
    )
}

fun PostDetailResponseDto.toDomain(): PostDetail {
    return PostDetail(
        id = id,
        caption = caption,
        imageUrl = imageUrl,
        contentUrl = contentUrl,
        mediaType = mediaType,
        author = FeedAuthor(
            id = author.id,
            username = author.username,
            profilePicUrl = author.profilePicUrl
        ),
        createdAt = createdAt,
        likeCount = likeCount,
        shareCount = shareCount,
        isLiked = isLiked,
        isBookmarked = isBookmarked,
        attachments = attachments.map {
            Attachment(
                id = it.id,
                contentUrl = it.contentUrl,
                thumbnailUrl = it.thumbnailUrl,
                mediaType = it.mediaType,
                displayOrder = it.displayOrder
            )
        }
    )
}

