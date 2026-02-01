package com.neerajsahu.flux.server.auth

import com.neerajsahu.flux.server.auth.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findBy_username(username: String): User?
}