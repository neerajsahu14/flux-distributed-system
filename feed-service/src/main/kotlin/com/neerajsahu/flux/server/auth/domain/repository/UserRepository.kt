package com.neerajsahu.flux.server.auth.domain.repository

import com.neerajsahu.flux.server.auth.domain.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findBy_username(username: String): User?

    @Query("SELECT u FROM User u WHERE LOWER(u._username) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchUsersByUsername(query: String, pageable: Pageable): Page<User>
}
