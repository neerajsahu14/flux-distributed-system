package com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("profileImageUrl") val profilePicUrl: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("isFollowing") val isFollowing: Boolean,
    @SerializedName("isFollowedBy") val isFollowedBy: Boolean
)