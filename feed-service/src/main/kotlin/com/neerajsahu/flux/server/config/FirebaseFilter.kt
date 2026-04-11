package com.neerajsahu.flux.server.config

import com.google.firebase.auth.FirebaseAuth
import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.auth.domain.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class FirebaseFilter(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

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

        val idToken = authHeader.substring(7)

        try {
            val decodedToken = firebaseAuth.verifyIdToken(idToken)
            val email = decodedToken.email
            
            var user = userRepository.findByEmail(email)

            if (user == null) {
                // Auto-sync Firebase user to local DB if they don't exist
                val name = decodedToken.name ?: email.substringBefore("@")
                val picture = decodedToken.picture
                
                user = userRepository.save(
                    User(
                        _username = name.replace(" ", "").lowercase() + "_" + UUID.randomUUID().toString().take(4),
                        email = email,
                        passwordHash = "", // Not needed with Firebase
                        bio = "New user via Firebase",
                        profilePicUrl = picture
                    )
                )
            }

            request.setAttribute("userId", user!!.id)

            if (SecurityContextHolder.getContext().authentication == null) {
                val authToken = UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.authorities
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }

        } catch (e: Exception) {
            logger.error("Firebase token verification failed: ${e.message}")
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}
