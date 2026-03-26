package com.neerajsahu.flux.androidclient.feature.relationship.di

import android.content.Context
import com.neerajsahu.flux.androidclient.core.datastore.TokenManager
import com.neerajsahu.flux.androidclient.feature.relationship.data.local.ProfileStatsDao
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.RelationshipApi
import com.neerajsahu.flux.androidclient.feature.relationship.data.repository.RelationshipRepositoryImpl
import com.neerajsahu.flux.androidclient.feature.relationship.domain.repository.RelationshipRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RelationshipModule {

    @Provides
    @Singleton
    fun provideRelationshipApi(retrofit: Retrofit): RelationshipApi {
        return retrofit.create(RelationshipApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRelationshipRepository(
        api: RelationshipApi,
        profileStatsDao: ProfileStatsDao,
        @ApplicationContext context: Context,
        tokenManager: TokenManager
    ): RelationshipRepository {
        return RelationshipRepositoryImpl(api, profileStatsDao, context, tokenManager)
    }
}
