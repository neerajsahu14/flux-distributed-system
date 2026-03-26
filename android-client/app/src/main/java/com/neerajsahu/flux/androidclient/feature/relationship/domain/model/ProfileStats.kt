package com.neerajsahu.flux.androidclient.feature.relationship.domain.model

data class ProfileStats(
    val userId: Long,
    val username: String,
    val fullName: String,
    val bio: String?,
    val postCount: Long,
    val followersCount: Long,
    val followingCount: Long,
    val isFollowing: Boolean
)
