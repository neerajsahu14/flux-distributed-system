package com.neerajsahu.flux.androidclient.feature.auth.di

import com.neerajsahu.flux.androidclient.feature.auth.data.repository.AuthRepositoryImpl
import com.neerajsahu.flux.androidclient.feature.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}