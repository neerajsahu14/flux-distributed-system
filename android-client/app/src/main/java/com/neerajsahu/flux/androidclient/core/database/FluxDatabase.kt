package com.neerajsahu.flux.androidclient.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neerajsahu.flux.androidclient.feature.auth.data.local.UserDao
import com.neerajsahu.flux.androidclient.feature.auth.data.local.UserEntity
import com.neerajsahu.flux.androidclient.feature.feed.data.local.PostDao
import com.neerajsahu.flux.androidclient.feature.feed.data.local.PostEntity
import com.neerajsahu.flux.androidclient.feature.interaction.data.local.InteractionDao
import com.neerajsahu.flux.androidclient.feature.interaction.data.local.InteractionEntity
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.PendingActionEntity
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.ProfileEntity
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.ProfileStatsDao
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.ProfileStatsEntity

@Database(
    entities = [
        UserEntity::class,
        ProfileStatsEntity::class,
        ProfileEntity::class,
        PendingActionEntity::class,
        PostEntity::class,
        InteractionEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class FluxDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val profileStatsDao: ProfileStatsDao
    abstract val postDao: PostDao
    abstract val interactionDao: InteractionDao
}
