package com.neerajsahu.flux.server.auth

import com.neerajsahu.flux.server.auth.model.AuthResponse
import com.neerajsahu.flux.server.auth.model.LoginRequest
import com.neerajsahu.flux.server.auth.model.RegisterRequest
import com.neerajsahu.flux.server.auth.model.UserResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
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