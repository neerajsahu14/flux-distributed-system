package com.neerajsahu.flux.androidclient.feature.feed.data.local

import androidx.room.Entity

@Entity(
    tableName = "feed_posts",
    primaryKeys = ["id", "scope"]
)
data class PostEntity(
    val id: Long,
    val scope: String,
    val caption: String?,
    val imageUrl: String,
    val authorId: Long,
    val authorUsername: String,
    val authorProfilePicUrl: String?,
    val createdAt: String,
    val likeCount: Int,
    val cachedAt: Long = System.currentTimeMillis()
)

