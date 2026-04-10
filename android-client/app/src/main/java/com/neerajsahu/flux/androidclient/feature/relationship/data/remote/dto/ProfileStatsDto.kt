package com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProfileStatsResponse(
    @SerializedName("profile") val profile: ProfileResponse,
    @SerializedName("postCount") val postCount: Long,
    @SerializedName("followersCount") val followersCount: Long,
    @SerializedName("followingCount") val followingCount: Long,
    @SerializedName("isFollowing") val isFollowing: Boolean
)
