package com.neerajsahu.flux.androidclient.feature.interaction.di

import com.neerajsahu.flux.androidclient.feature.feed.data.local.PostDao
import com.neerajsahu.flux.androidclient.feature.interaction.data.local.InteractionDao
import com.neerajsahu.flux.androidclient.feature.interaction.data.remote.InteractionApi
import com.neerajsahu.flux.androidclient.feature.interaction.data.repository.InteractionRepositoryImpl
import com.neerajsahu.flux.androidclient.feature.interaction.data.repository.RoomInteractionSyncSource
import com.neerajsahu.flux.androidclient.feature.interaction.domain.repository.InteractionRepository
import com.neerajsahu.flux.androidclient.feature.interaction.domain.repository.InteractionSyncSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InteractionModule {

    @Provides
    @Singleton
    fun provideInteractionApi(retrofit: Retrofit): InteractionApi {
        return retrofit.create(InteractionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideInteractionRepository(
        interactionApi: InteractionApi,
        interactionDao: InteractionDao,
        postDao: PostDao
    ): InteractionRepository {
        return InteractionRepositoryImpl(interactionApi, interactionDao, postDao)
    }

    @Provides
    @Singleton
    fun provideInteractionSyncSource(interactionDao: InteractionDao): InteractionSyncSource {
        return RoomInteractionSyncSource(interactionDao)
    }
}



