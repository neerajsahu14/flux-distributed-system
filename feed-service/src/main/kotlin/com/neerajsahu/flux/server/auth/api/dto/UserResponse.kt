package com.neerajsahu.flux.server.auth.api.dto

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val bio: String?,
    val profilePicUrl: String?
)