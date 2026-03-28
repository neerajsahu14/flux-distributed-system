package com.neerajsahu.flux.androidclient.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.neerajsahu.flux.androidclient.core.database.FluxDatabase
import com.neerajsahu.flux.androidclient.feature.auth.data.local.UserDao
import com.neerajsahu.flux.androidclient.feature.feed.data.local.PostDao
import com.neerajsahu.flux.androidclient.feature.interaction.data.local.InteractionDao
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.ProfileStatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("flux_prefs") }
        )
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FluxDatabase {
        return Room.databaseBuilder(
            context,
            FluxDatabase::class.java,
            "flux_db"
        )
        .fallbackToDestructiveMigration() // Added for development as version changed
        .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: FluxDatabase): UserDao {
        return database.userDao
    }

    @Provides
    @Singleton
    fun provideProfileStatsDao(database: FluxDatabase): ProfileStatsDao {
        return database.profileStatsDao
    }

    @Provides
    @Singleton
    fun providePostDao(database: FluxDatabase): PostDao {
        return database.postDao
    }

    @Provides
    @Singleton
    fun provideInteractionDao(database: FluxDatabase): InteractionDao {
        return database.interactionDao
    }
}
