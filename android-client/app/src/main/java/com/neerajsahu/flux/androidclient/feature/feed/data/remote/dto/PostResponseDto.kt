package com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PostResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("caption") val caption: String?,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("author") val author: FeedAuthorDto,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("likeCount") val likeCount: Int,
    @SerializedName("shareCount") val shareCount: Int? = null,
    @SerializedName("isLiked") val isLiked: Boolean? = null,
    @SerializedName("isBookmarked") val isBookmarked: Boolean? = null
)

data class UpdatePostRequestDto(
    @SerializedName("caption") val caption: String?
)
