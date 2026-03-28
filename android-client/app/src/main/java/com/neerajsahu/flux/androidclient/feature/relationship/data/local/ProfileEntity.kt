package com.neerajsahu.flux.androidclient.feature.relationship.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: Long,
    val username: String,
    val fullName: String,
    val profilePicUrl: String?,
    val bio: String?,
    val isFollowing: Boolean,
    val isFollowedBy: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)
