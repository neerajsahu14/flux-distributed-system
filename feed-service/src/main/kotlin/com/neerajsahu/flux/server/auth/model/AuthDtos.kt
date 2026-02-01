package com.neerajsahu.flux.server.auth.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// 1. Register Request
// Note: Maine 'fullName' add kiya hai kyunki AuthService usse use kar raha hai
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

// 2. Login Request
data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

// 3. Auth Response
// Note: Isse FLATTEN kar diya hai taaki AuthService ke simple return se match kare
data class AuthResponse(
    val token: String,
    val userId: Long,
    val username: String
)

// 4. User Response (Agar /me endpoint use kar rahe ho)
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val bio: String?,
    val profilePicUrl: String?
)