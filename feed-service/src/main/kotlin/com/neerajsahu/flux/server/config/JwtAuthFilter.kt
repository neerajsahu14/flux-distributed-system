package com.neerajsahu.flux.server.config

import com.neerajsahu.flux.server.auth.domain.repository.UserRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets

@Component
class JwtAuthFilter(
    private val userRepository: UserRepository,
    @Value("\${jwt.secret:dev-secret-please-change-to-something-very-long-and-secure}") private val jwtSecret: String
) : OncePerRequestFilter() {


    private val key by lazy { Keys.hmacShaKeyFor(jwtSecret.toByteArray(StandardCharsets.UTF_8)) }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        // 1. Basic Validation
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt = authHeader.substring(7)

        try {
            // 2. Token Parsing
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .payload

            val userId = claims.subject.toLong()

            // 3. Authentication Check
            if (userId != null && SecurityContextHolder.getContext().authentication == null) {


                val user = userRepository.findById(userId).orElse(null)

                if (user != null) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        user, // Principal: whole User object
                        null, // Credentials: Null (dine JWT verify)
                        user.authorities
                    )

                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)


                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        } catch (e: Exception) {

        }

        filterChain.doFilter(request, response)
    }
}