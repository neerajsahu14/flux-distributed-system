package com.neerajsahu.flux.server.relationship.domain.repository

import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.relationship.domain.model.Follow
import com.neerajsahu.flux.server.relationship.domain.model.FollowId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FollowRepository : JpaRepository<Follow, FollowId> {

    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END 
        FROM Follow f 
        WHERE f.id.followerId = :followerId 
        AND f.id.followeeId = :followeeId 
        AND f.isValid = true
    """)
    fun isFollowing(followerId: Long, followeeId: Long): Boolean

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.id.followeeId = :userId AND f.isValid = true")
    fun countFollowers(userId: Long): Long

    @Query("SELECT COUNT(f) FROM Follow f WHERE f.id.followerId = :userId AND f.isValid = true")
    fun countFollowing(userId: Long): Long

    @Query("""
        SELECT f.follower FROM Follow f 
        WHERE f.id.followeeId = :userId 
        AND f.isValid = true 
        ORDER BY f.createdAt DESC
    """)
    fun findFollowersByUserId(userId: Long, pageable: Pageable): Page<User>

    @Query("""
        SELECT f.followee FROM Follow f 
        WHERE f.id.followerId = :userId 
        AND f.isValid = true 
        ORDER BY f.createdAt DESC
    """)
    fun findFollowingByUserId(userId: Long, pageable: Pageable): Page<User>

    @Query("SELECT f FROM Follow f WHERE f.id.followerId = :followerId AND f.id.followeeId = :followeeId")
    fun findRelationship(followerId: Long, followeeId: Long): Optional<Follow>
}