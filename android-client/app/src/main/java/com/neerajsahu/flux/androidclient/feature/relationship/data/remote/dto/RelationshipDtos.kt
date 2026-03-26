package com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProfileStatsResponse(
    @SerializedName("profile") val profile: Profile,
    @SerializedName("postCount") val postCount: Long,
    @SerializedName("followersCount") val followersCount: Long,
    @SerializedName("followingCount") val followingCount: Long,
    @SerializedName("isFollowing") val isFollowing: Boolean
)

data class Profile(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("bio") val bio: String?
)

data class FollowActionResponse(
    @SerializedName("status") val status: String,
    @SerializedName("targetUserId") val targetUserId: Long
)

data class RelationshipInfoResponse(
    @SerializedName("followersCount") val followersCount: Long,
    @SerializedName("followingCount") val followingCount: Long,
    @SerializedName("isFollowing") val isFollowing: Boolean
)

data class FollowRequest(
    @SerializedName("requestId") val requestId: String
)
