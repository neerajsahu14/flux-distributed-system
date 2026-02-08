package com.neerajsahu.flux.server.auth.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 chars")
    val username: String,

    @field:NotBlank
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank
    @field:Size(min = 6, max = 128, message = "Password must be at least 6 chars")
    val password: String,

    val fullName: String? = null, // <-- Added for compatibility
    val bio: String? = null,
    val profilePicUrl: String? = null
)