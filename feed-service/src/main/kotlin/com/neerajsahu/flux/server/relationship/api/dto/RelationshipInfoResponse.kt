package com.neerajsahu.flux.server.relationship.api.dto

// Jab follow/unfollow button dabayenge to ye response aayega
data class FollowActionResponse(
    val status: String,   // "Followed" or "Unfollowed"
    val targetUserId: Long
)

// Profile page ke header ke liye (Counts + Button State)
data class RelationshipInfoResponse(
    val followersCount: Long,
    val followingCount: Long,
    val isFollowing: Boolean // Button "Follow" dikhaye ya "Unfollow"?
)