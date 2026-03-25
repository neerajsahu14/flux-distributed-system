package com.neerajsahu.flux.androidclient.feature.auth.domain.repository

import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.RegisterRequestDto
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.UserDto

interface AuthRepository {
    suspend fun login(email: String, password: String): AppResult<Unit>
    suspend fun signup(request: RegisterRequestDto): AppResult<Unit>
    suspend fun getProfile(): AppResult<UserDto>
}
