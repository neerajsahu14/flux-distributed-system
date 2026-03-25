package com.neerajsahu.flux.androidclient.feature.auth.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // UPSERT: Agar user pehle se hai, toh usko naye data se replace kar de.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // BULK UPSERT: Jab feed load hogi toh 20 alag authors aayenge, unhe ek sath daalna hai
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    // FLOW: Ye sabse important hai.
    // UI isko observe karega. Agar user DB me update hua, UI automatically refresh ho jayega.
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Long): Flow<UserEntity?>

    // Logout karne ke baad data saaf karna zaroori hai
    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}