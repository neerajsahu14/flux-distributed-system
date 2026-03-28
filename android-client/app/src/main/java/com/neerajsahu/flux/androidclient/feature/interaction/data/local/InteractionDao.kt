package com.neerajsahu.flux.androidclient.feature.interaction.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionDao {

    @Query("SELECT * FROM interaction_state")
    fun observeAll(): Flow<List<InteractionEntity>>

    @Query("SELECT * FROM interaction_state")
    suspend fun getAll(): List<InteractionEntity>

    @Query("SELECT * FROM interaction_state WHERE postId = :postId LIMIT 1")
    suspend fun getByPostId(postId: Long): InteractionEntity?

    @Query("SELECT * FROM interaction_state WHERE postId IN (:postIds)")
    suspend fun getByPostIds(postIds: List<Long>): List<InteractionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: InteractionEntity)

    @Transaction
    suspend fun updateLiked(postId: Long, isLiked: Boolean) {
        val existing = getByPostId(postId)
        upsert(
            InteractionEntity(
                postId = postId,
                isLiked = isLiked,
                isBookmarked = existing?.isBookmarked ?: false,
                likeCount = existing?.likeCount ?: 0,
                shareCount = existing?.shareCount ?: 0
            )
        )
    }

    @Transaction
    suspend fun updateBookmarked(postId: Long, isBookmarked: Boolean) {
        val existing = getByPostId(postId)
        upsert(
            InteractionEntity(
                postId = postId,
                isLiked = existing?.isLiked ?: false,
                isBookmarked = isBookmarked,
                likeCount = existing?.likeCount ?: 0,
                shareCount = existing?.shareCount ?: 0
            )
        )
    }
}


