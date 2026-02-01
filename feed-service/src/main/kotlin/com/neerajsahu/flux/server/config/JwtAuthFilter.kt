package com.neerajsahu.flux.server.config

import com.neerajsahu.flux.server.auth.UserRepository
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

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt = authHeader.substring(7)
        try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .payload

            val userId = claims.subject.toLong()

            if (SecurityContextHolder.getContext().authentication == null) {
                val user = userRepository.findById(userId).orElseThrow()
                val authToken = UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.authorities
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken

                // Add user ID to request attributes for easy access in controllers
                request.setAttribute("userId", userId)
            }
        } catch (e: Exception) {
            // Handle invalid token
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}
