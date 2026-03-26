package com.neerajsahu.flux.androidclient.feature.relationship.domain.model

data class RelationshipUser(
    val id: Long,
    val username: String,
    val fullName: String,
    val profilePicUrl: String?,
    val isFollowing: Boolean,
    val isFollowedBy: Boolean
)
