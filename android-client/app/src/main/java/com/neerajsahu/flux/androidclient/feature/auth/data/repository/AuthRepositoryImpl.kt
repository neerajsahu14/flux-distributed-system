package com.neerajsahu.flux.androidclient.feature.auth.data.repository

import com.neerajsahu.flux.androidclient.core.datastore.TokenManager
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.auth.data.local.UserDao
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.AuthApi
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.LoginRequestDto
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.RegisterRequestDto
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.UpdateBioRequest
import com.neerajsahu.flux.androidclient.feature.auth.domain.model.User
import com.neerajsahu.flux.androidclient.feature.auth.domain.repository.AuthRepository
import com.neerajsahu.flux.androidclient.feature.auth.mapper.toUser
import com.neerajsahu.flux.androidclient.feature.auth.mapper.toUserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun login(email: String, password: String): AppResult<Unit> {
        return try {
            val response = authApi.login(LoginRequestDto(email, password))
            tokenManager.saveToken(response.token)
            tokenManager.saveUserId(response.userId)
            fetchAndStoreProfile()
            AppResult.Success(Unit)
        } catch (e: HttpException) {
            AppResult.Error(e.response()?.errorBody()?.string() ?: "An unknown error occurred")
        } catch (e: IOException) {
            AppResult.Error("Couldn't reach server. Check your internet connection.")
        }
    }

    override suspend fun signup(request: RegisterRequestDto): AppResult<Unit> {
        return try {
            val response = authApi.register(request)
            tokenManager.saveToken(response.token)
            tokenManager.saveUserId(response.userId)
            fetchAndStoreProfile()
            AppResult.Success(Unit)
        } catch (e: HttpException) {
            AppResult.Error(e.response()?.errorBody()?.string() ?: "An unknown error occurred")
        } catch (e: IOException) {
            AppResult.Error("Couldn't reach server. Check your internet connection.")
        }
    }

    override suspend fun fetchAndStoreProfile(): AppResult<Unit> {
        return try {
            val token = tokenManager.getToken().first()
            if (token.isNullOrBlank()) {
                return AppResult.Error("Not authenticated.")
            }
            val userDto = authApi.getProfile("Bearer $token")
            userDao.insertUser(userDto.toUserEntity())
            AppResult.Success(Unit)
        } catch (e: HttpException) {
            AppResult.Error(e.response()?.errorBody()?.string() ?: "An unknown error occurred")
        } catch (e: IOException) {
            AppResult.Error("Couldn't reach server. Check your internet connection.")
        }
    }

    override fun getProfile(): Flow<User?> {
        return tokenManager.getUserId().flatMapLatest { userId ->
            if (userId != null) {
                userDao.getUserById(userId).map { it?.toUser() }
            } else {
                flowOf(null)
            }
        }
    }

    override suspend fun updateBio(bio: String): AppResult<User> {
        return try {
            val userDto = authApi.updateBio(UpdateBioRequest(bio))
            userDao.insertUser(userDto.toUserEntity())
            AppResult.Success(userDto.toUser())
        } catch (e: HttpException) {
            AppResult.Error(e.response()?.errorBody()?.string() ?: "An unknown error occurred")
        } catch (e: IOException) {
            AppResult.Error("Couldn't reach server. Check your internet connection.")
        }
    }

    override suspend fun updateProfileImage(imageFile: File): AppResult<User> {
        return try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val userDto = authApi.updateProfileImage(body)
            userDao.insertUser(userDto.toUserEntity())
            AppResult.Success(userDto.toUser())
        } catch (e: HttpException) {
            AppResult.Error(e.response()?.errorBody()?.string() ?: "An unknown error occurred")
        } catch (e: IOException) {
            AppResult.Error("Couldn't reach server. Check your internet connection.")
        }
    }
}
