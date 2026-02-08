package com.neerajsahu.flux.server.auth.domain.repository

import com.neerajsahu.flux.server.auth.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findBy_username(username: String): User?
}