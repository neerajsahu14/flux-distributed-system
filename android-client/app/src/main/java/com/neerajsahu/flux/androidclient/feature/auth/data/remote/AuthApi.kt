package com.neerajsahu.flux.androidclient.feature.auth.data.remote

import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.AuthResponseDto
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.LoginRequestDto
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.RegisterRequestDto
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.UserDto
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.UpdateBioRequest
import okhttp3.MultipartBody
import retrofit2.http.*

interface AuthApi {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @GET("api/v1/auth/me")
    suspend fun getProfile(@Header("Authorization") token: String): UserDto

    @PATCH("api/v1/auth/profile/bio")
    suspend fun updateBio(@Body request: UpdateBioRequest): UserDto

    @Multipart
    @PUT("api/v1/auth/profile/image")
    suspend fun updateProfileImage(@Part file: MultipartBody.Part): UserDto
}
