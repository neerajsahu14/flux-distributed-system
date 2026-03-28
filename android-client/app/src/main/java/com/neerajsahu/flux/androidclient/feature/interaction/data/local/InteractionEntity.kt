package com.neerajsahu.flux.androidclient.feature.interaction.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interaction_state")
data class InteractionEntity(
    @PrimaryKey
    val postId: Long,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val likeCount: Int = 0,
    val shareCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)


