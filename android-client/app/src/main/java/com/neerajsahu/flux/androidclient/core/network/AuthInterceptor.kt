package com.neerajsahu.flux.androidclient.core.network

import com.neerajsahu.flux.androidclient.core.datastore.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getTokenSync()

        val requestBuilder = chain.request().newBuilder()
        if (token != null) {
            println("AuthInterceptor: Adding Token")
            requestBuilder.addHeader("Authorization", "Bearer $token")
        } else {
            println("AuthInterceptor: Token is NULL")
        }
        val request = requestBuilder.build()
        println("AuthInterceptor: Proceeding with request: ${request.url}")
        
        val response = chain.proceed(request)
        if (response.code == 401) {
            println("AuthInterceptor: 401 Unauthorized for ${request.url}")
            AuthEventManager.triggerUnauthorizedEvent()
        }
        return response
    }
}
