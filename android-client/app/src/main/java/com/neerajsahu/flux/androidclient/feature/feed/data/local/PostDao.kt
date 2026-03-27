package com.neerajsahu.flux.androidclient.feature.feed.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PostDao {

    @Query("SELECT * FROM feed_posts WHERE scope = :scope ORDER BY createdAt DESC")
    suspend fun getPostsByScope(scope: String): List<PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPosts(posts: List<PostEntity>)

    @Query("DELETE FROM feed_posts WHERE scope = :scope")
    suspend fun clearScope(scope: String)

    @Query("DELETE FROM feed_posts")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceScope(scope: String, posts: List<PostEntity>) {
        clearScope(scope)
        if (posts.isNotEmpty()) {
            upsertPosts(posts)
        }
    }
}

