package com.neerajsahu.flux.androidclient.feature.relationship.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileStatsDao {

    // Profile Stats
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileStats(stats: ProfileStatsEntity)

    @Query("SELECT * FROM profile_stats WHERE userId = :userId LIMIT 1")
    fun getProfileStatsById(userId: Long): Flow<ProfileStatsEntity?>

    @Query("UPDATE profile_stats SET lastUpdated = :timestamp WHERE userId = :userId")
    suspend fun updateLastAccessed(userId: Long, timestamp: Long = System.currentTimeMillis())

    // Profiles (for search and connections)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<ProfileEntity>)

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE id IN (:ids)")
    fun getProfilesByIds(ids: List<Long>): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE username LIKE '%' || :query || '%' OR fullName LIKE '%' || :query || '%' LIMIT :limit")
    fun searchProfiles(query: String, limit: Int = 50): Flow<List<ProfileEntity>>

    @Query("DELETE FROM profile_stats WHERE userId = :userId")
    suspend fun deleteProfileStatsById(userId: Long)

    @Query("DELETE FROM profile_stats")
    suspend fun clearAllProfileStats()

    @Query("DELETE FROM profiles")
    suspend fun clearAllProfiles()

    // LRU Cleanup for Profile Stats
    @Query("""
        DELETE FROM profile_stats 
        WHERE userId != :currentUserId 
        AND userId NOT IN (
            SELECT userId FROM profile_stats 
            WHERE userId != :currentUserId 
            ORDER BY lastUpdated DESC 
            LIMIT :limit
        )
    """)
    suspend fun pruneOldProfileStats(currentUserId: Long, limit: Int = 20)

    // LRU Cleanup for Profiles
    @Query("""
        DELETE FROM profiles 
        WHERE id != :currentUserId 
        AND id NOT IN (
            SELECT id FROM profiles 
            WHERE id != :currentUserId 
            ORDER BY lastUpdated DESC 
            LIMIT :limit
        )
    """)
    suspend fun pruneOldProfiles(currentUserId: Long, limit: Int = 50)

    // Pending Actions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingAction(action: PendingActionEntity)

    @Query("SELECT * FROM pending_actions ORDER BY createdAt ASC")
    suspend fun getAllPendingActions(): List<PendingActionEntity>

    @Delete
    suspend fun deletePendingAction(action: PendingActionEntity)
}
