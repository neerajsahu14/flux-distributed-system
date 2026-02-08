package com.neerajsahu.flux.server.auth.api

import com.neerajsahu.flux.server.auth.service.AuthService
import com.neerajsahu.flux.server.auth.domain.repository.UserRepository
import com.neerajsahu.flux.server.auth.api.dto.AuthResponse
import com.neerajsahu.flux.server.auth.api.dto.LoginRequest
import com.neerajsahu.flux.server.auth.api.dto.RegisterRequest
import com.neerajsahu.flux.server.auth.api.dto.UserResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val userRepository: UserRepository,
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.register(req))

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<AuthResponse> =
        ResponseEntity.ok(authService.login(req))

    @GetMapping("/me")
    fun me(@RequestAttribute("userId") userId: Long): ResponseEntity<UserResponse> {
        val user = userRepository.findById(userId).orElseThrow()
        return ResponseEntity.ok(authService.getUserResponse(user))
    }
}