package com.neerajsahu.flux.androidclient.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neerajsahu.flux.androidclient.feature.auth.data.local.UserDao
import com.neerajsahu.flux.androidclient.feature.auth.data.local.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FluxDatabase : RoomDatabase() {
    abstract val userDao: UserDao
}
