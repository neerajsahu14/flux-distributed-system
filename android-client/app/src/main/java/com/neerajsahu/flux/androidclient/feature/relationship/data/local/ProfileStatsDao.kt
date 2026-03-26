package com.neerajsahu.flux.androidclient.feature.relationship.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileStats(stats: ProfileStatsEntity)

    @Query("SELECT * FROM profile_stats WHERE userId = :userId LIMIT 1")
    fun getProfileStatsById(userId: Long): Flow<ProfileStatsEntity?>

    @Query("UPDATE profile_stats SET lastAccessed = :timestamp WHERE userId = :userId")
    suspend fun updateLastAccessed(userId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM profile_stats WHERE userId = :userId")
    suspend fun deleteProfileStatsById(userId: Long)

    @Query("DELETE FROM profile_stats")
    suspend fun clearAllProfileStats()

    // LRU Cleanup: Delete profiles except the current user, keeping only the top N most recently accessed
    @Query("""
        DELETE FROM profile_stats 
        WHERE userId != :currentUserId 
        AND userId NOT IN (
            SELECT userId FROM profile_stats 
            WHERE userId != :currentUserId 
            ORDER BY lastAccessed DESC 
            LIMIT :limit
        )
    """)
    suspend fun pruneOldProfiles(currentUserId: Long, limit: Int = 20)

    // Pending Actions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingAction(action: PendingActionEntity)

    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC")
    suspend fun getAllPendingActions(): List<PendingActionEntity>

    @Delete
    suspend fun deletePendingAction(action: PendingActionEntity)
}
