package com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto

import com.google.gson.annotations.SerializedName


data class AttachmentResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("contentUrl") val contentUrl: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName("mediaType") val mediaType: String,
    @SerializedName("displayOrder") val displayOrder: Int
)
