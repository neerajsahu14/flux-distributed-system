package com.neerajsahu.flux.androidclient.feature.relationship.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_stats")
data class ProfileStatsEntity(
    @PrimaryKey val userId: Long,
    val username: String,
    val fullName: String,
    val bio: String?,
    val postCount: Long,
    val followersCount: Long,
    val followingCount: Long,
    val isFollowing: Boolean,
    val lastUpdated: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis()
)
