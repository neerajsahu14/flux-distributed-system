package com.neerajsahu.flux.server.relationship.api.dto

data class ProfileResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val profileImageUrl: String?,
    val bio: String?,
    val isFollowing: Boolean,
    val isFollowedBy: Boolean
)
