package com.neerajsahu.flux.server.auth.model

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant // NOTE: Java Time use karna hai, Kotlin Time nahi

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "username", nullable = false, unique = true, length = 50)
    val _username: String = "",

    @Column(nullable = false, unique = true, length = 100)
    val email: String = "",

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String = "",

    val bio: String? = null,

    @Column(name = "profile_pic_url")
    val profilePicUrl: String? = null,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
) : UserDetails {

    // Spring Security Methods
    override fun getAuthorities(): Collection<GrantedAuthority> = listOf(SimpleGrantedAuthority("ROLE_USER"))
    override fun getPassword(): String = passwordHash
    override fun getUsername(): String = email // Login email se hoga
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
}