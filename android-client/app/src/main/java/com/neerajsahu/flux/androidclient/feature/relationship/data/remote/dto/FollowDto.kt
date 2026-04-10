package com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FollowActionResponse(
    @SerializedName("status") val status: String,
    @SerializedName("targetUserId") val targetUserId: Long
)

data class FollowRequest(
    @SerializedName("requestId") val requestId: String
)