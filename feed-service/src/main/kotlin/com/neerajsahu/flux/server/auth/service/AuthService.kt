package com.neerajsahu.flux.server.auth.service

import com.cloudinary.Cloudinary
import com.cloudinary.Transformation
import com.neerajsahu.flux.server.auth.api.dto.AuthResponse
import com.neerajsahu.flux.server.auth.api.dto.LoginRequest
import com.neerajsahu.flux.server.auth.api.dto.RegisterRequest
import com.neerajsahu.flux.server.auth.api.dto.UserResponse
import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.auth.domain.repository.UserRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager, // Login check ke liye
    private val cloudinary: Cloudinary,
    @Value("\${jwt.secret:dev-secret-please-change-to-something-very-long-and-secure}") private val jwtSecret: String,
    @Value("\${jwt.expiration:86400}") private val jwtExpirationSeconds: Long,
) {
    private val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray(StandardCharsets.UTF_8))

    fun register(req: RegisterRequest): AuthResponse {
        // Fix: Exception throw karo agar user already hai
        if (userRepository.findByEmail(req.email) != null) {
            throw RuntimeException("Email already taken")
        }
        if (userRepository.findBy_username(req.username) != null) {
            throw RuntimeException("Username already taken")
        }

        val saved = userRepository.save(
            User(
                _username = req.username,
                email = req.email,
                passwordHash = passwordEncoder.encode(req.password)!!,
                bio = req.bio,
                profilePicUrl = null
            )
        )
        return issueToken(saved)
    }

    fun login(req: LoginRequest): AuthResponse {
        // Authentication Manager handles password matching
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(req.email, req.password)
        )

        val user = userRepository.findByEmail(req.email)!!
        return issueToken(user)
    }

    private fun issueToken(user: User): AuthResponse {
        val now = Instant.now()
        val exp = now.plusSeconds(jwtExpirationSeconds)
        val jwt = Jwts.builder()
            .subject(user.id!!.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .claim("username", user._username)
            .claim("email", user.email)
            .signWith(key)
            .compact()

        return AuthResponse(
            token = jwt,
            userId = user.id,
            username = user._username
        )
    }

    fun updateBio(user: User, bio: String): UserResponse {
        user.bio = bio
        val updatedUser = userRepository.save(user)
        return getUserResponse(updatedUser)
    }

    fun updateProfileImage(user: User, file: MultipartFile): UserResponse {
        val uploadParams = mapOf(
            "folder" to "flux-feed/profiles",
            "resource_type" to "image"
        )

        val uploadResult = cloudinary.uploader().upload(file.bytes, uploadParams)
        val publicId = uploadResult["public_id"] as String
        
        val thumbnailUrl = cloudinary.url()
            .transformation(
                Transformation<Transformation<*>>()
                    .width(200)
                    .height(200)
                    .crop("fill")
                    .gravity("face")
                    .quality("auto")
                    .fetchFormat("auto")
            )
            .generate(publicId)

        user.profilePicUrl = thumbnailUrl
        val updatedUser = userRepository.save(user)
        return getUserResponse(updatedUser)
    }

    // Helper method (used in controllers)
    fun getUserResponse(user: User) = UserResponse(
        id = user.id!!,
        username = user._username,
        email = user.email,
        bio = user.bio,
        profilePicUrl = user.profilePicUrl,
    )
}