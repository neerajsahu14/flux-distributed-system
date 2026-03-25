package com.neerajsahu.flux.androidclient.feature.auth.data.repository

import com.neerajsahu.flux.androidclient.core.datastore.TokenManager
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.AuthApi
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.LoginRequestDto
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.RegisterRequestDto
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.UserDto
import com.neerajsahu.flux.androidclient.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import java.util.NoSuchElementException
import javax.inject.Inject
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): AppResult<Unit> {
        return try {
            val response = authApi.login(LoginRequestDto(email, password))
            tokenManager.saveToken(response.token)
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
            AppResult.Success(Unit)
        } catch (e: HttpException) {
            AppResult.Error(e.response()?.errorBody()?.string() ?: "An unknown error occurred")
        } catch (e: IOException) {
            AppResult.Error("Couldn't reach server. Check your internet connection.")
        }
    }

    override suspend fun getProfile(): AppResult<UserDto> {
        return try {
            val token = tokenManager.getToken().first()
            if (token.isNullOrBlank()) {
                return AppResult.Error("Not authenticated.")
            }
            val user = authApi.getProfile("Bearer $token")
            AppResult.Success(user)
        } catch (e: HttpException) {
            AppResult.Error(e.response()?.errorBody()?.string() ?: "An unknown error occurred")
        } catch (e: IOException) {
            AppResult.Error("Couldn't reach server. Check your internet connection.")
        } catch (e: NoSuchElementException) {
            AppResult.Error("Not authenticated.")
        }
    }
}