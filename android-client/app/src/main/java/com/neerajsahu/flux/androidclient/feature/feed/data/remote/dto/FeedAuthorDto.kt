package com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FeedAuthorDto(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("profilePicUrl") val profilePicUrl: String?
)