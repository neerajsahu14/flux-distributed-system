package com.neerajsahu.flux.androidclient.feature.feed.di

import com.neerajsahu.flux.androidclient.feature.feed.data.local.PostDao
import com.neerajsahu.flux.androidclient.feature.feed.data.remote.FeedApi
import com.neerajsahu.flux.androidclient.feature.feed.data.repository.FeedRepositoryImpl
import com.neerajsahu.flux.androidclient.feature.feed.domain.repository.FeedRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeedModule {

    @Provides
    @Singleton
    fun provideFeedApi(retrofit: Retrofit): FeedApi {
        return retrofit.create(FeedApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFeedRepository(
        feedApi: FeedApi,
        postDao: PostDao,
        errorParser: com.neerajsahu.flux.androidclient.core.utils.ErrorParser
    ): FeedRepository {
        return FeedRepositoryImpl(feedApi, postDao, errorParser)
    }
}

