package com.neerajsahu.flux.androidclient.feature.auth.domain.repository

import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.RegisterRequestDto
import com.neerajsahu.flux.androidclient.feature.auth.domain.model.User
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AuthRepository {
    suspend fun login(email: String, password: String): AppResult<Unit>
    suspend fun signup(request: RegisterRequestDto): AppResult<Unit>
    suspend fun fetchAndStoreProfile(): AppResult<Unit>
    fun getProfile(): Flow<User?>
    
    suspend fun updateBio(bio: String): AppResult<User>
    suspend fun updateProfileImage(imageFile: File): AppResult<User>
}
