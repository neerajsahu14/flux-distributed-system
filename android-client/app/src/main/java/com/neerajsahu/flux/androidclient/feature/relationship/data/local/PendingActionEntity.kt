package com.neerajsahu.flux.androidclient.feature.relationship.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val targetUserId: Long,
    val actionType: String, // "FOLLOW" or "UNFOLLOW"
    val requestId: String,
    val createdAt: Long = System.currentTimeMillis()
)
