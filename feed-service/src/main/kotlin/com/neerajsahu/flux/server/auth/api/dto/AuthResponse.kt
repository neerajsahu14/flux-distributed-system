package com.neerajsahu.flux.server.auth.api.dto

data class AuthResponse(
    val token: String,
    val userId: Long,
    val username: String
)