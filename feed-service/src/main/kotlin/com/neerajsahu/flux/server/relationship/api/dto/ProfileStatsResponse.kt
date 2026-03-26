package com.neerajsahu.flux.server.relationship.api.dto


data class ProfileStatsResponse(
    val profile: Profile,
    val postCount: Long,
    val followersCount: Long,
    val followingCount: Long,
    val isFollowing: Boolean
)

data class Profile(
    val id: Long,
    val username: String,
    val fullName: String,
    val bio: String
)