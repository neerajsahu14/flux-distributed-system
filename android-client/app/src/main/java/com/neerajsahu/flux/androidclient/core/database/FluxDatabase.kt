package com.neerajsahu.flux.androidclient.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neerajsahu.flux.androidclient.feature.auth.data.local.UserDao
import com.neerajsahu.flux.androidclient.feature.auth.data.local.UserEntity
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.PendingActionEntity
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.ProfileStatsDao
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.ProfileStatsEntity

@Database(
    entities = [UserEntity::class, ProfileStatsEntity::class, PendingActionEntity::class],
    version = 4,
    exportSchema = false
)
abstract class FluxDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val profileStatsDao: ProfileStatsDao
}
