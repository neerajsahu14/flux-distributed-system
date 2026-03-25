package com.neerajsahu.flux.androidclient.feature.auth.data.remote

import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.AuthResponseDto
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.LoginRequestDto
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.RegisterRequestDto
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @GET("api/v1/auth/me")
    suspend fun getProfile(@Header("Authorization") token: String): UserDto
}