package com.neerajsahu.flux.server.relationship.api.dto


data class ProfileStatsResponse(
    val profile: ProfileResponse,
    val postCount: Long,
    val followersCount: Long,
    val followingCount: Long,
    val isFollowing: Boolean
)
