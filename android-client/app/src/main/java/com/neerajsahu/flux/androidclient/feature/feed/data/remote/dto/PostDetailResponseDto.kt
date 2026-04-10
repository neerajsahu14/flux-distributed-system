package com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto

import com.google.gson.annotations.SerializedName

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