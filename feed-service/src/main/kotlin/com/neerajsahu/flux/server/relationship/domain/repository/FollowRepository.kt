package com.neerajsahu.flux.server.relationship.domain.repository

import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.relationship.api.dto.ProfileResponse
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

    fun existsByRequestId(requestId: String): Boolean

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

    @Query("SELECT f FROM Follow f WHERE f.id.followerId = :followerId AND f.id.followeeId = :followeeId")
    fun findRelationship(followerId: Long, followeeId: Long): Optional<Follow>

    @Query(
        """
            SELECT new com.neerajsahu.flux.server.relationship.api.dto.ProfileResponse(
                u.id,
                u._username,
                u._username,
                u.profilePicUrl,
                u.bio,
                CASE WHEN cf.id.followerId IS NOT NULL THEN true ELSE false END,
                CASE WHEN cb.id.followerId IS NOT NULL THEN true ELSE false END
            )
            FROM User u
            LEFT JOIN Follow cf ON cf.id.followerId = :currentUserId AND cf.id.followeeId = u.id AND cf.isValid = true
            LEFT JOIN Follow cb ON cb.id.followerId = u.id AND cb.id.followeeId = :currentUserId AND cb.isValid = true
            WHERE u.id = :targetUserId
        """
    )
    fun findProfileWithStatus(targetUserId: Long, currentUserId: Long): ProfileResponse?

    @Query(
        value = """
            SELECT new com.neerajsahu.flux.server.relationship.api.dto.ProfileResponse(
                u.id,
                u._username,
                u._username,
                u.profilePicUrl,
                u.bio,
                CASE WHEN cf.id.followerId IS NOT NULL THEN true ELSE false END,
                CASE WHEN cb.id.followerId IS NOT NULL THEN true ELSE false END
            )
            FROM Follow f
            JOIN f.follower u
            LEFT JOIN Follow cf ON cf.id.followerId = :currentUserId AND cf.id.followeeId = u.id AND cf.isValid = true
            LEFT JOIN Follow cb ON cb.id.followerId = u.id AND cb.id.followeeId = :currentUserId AND cb.isValid = true
            WHERE f.id.followeeId = :targetUserId AND f.isValid = true
            ORDER BY f.createdAt DESC
        """,
        countQuery = """
            SELECT COUNT(f)
            FROM Follow f
            WHERE f.id.followeeId = :targetUserId AND f.isValid = true
        """
    )
    fun findFollowerProfilesWithStatus(targetUserId: Long, currentUserId: Long, pageable: Pageable): Page<ProfileResponse>

    @Query(
        value = """
            SELECT new com.neerajsahu.flux.server.relationship.api.dto.ProfileResponse(
                u.id,
                u._username,
                u._username,
                u.profilePicUrl,
                u.bio,
                CASE WHEN cf.id.followerId IS NOT NULL THEN true ELSE false END,
                CASE WHEN cb.id.followerId IS NOT NULL THEN true ELSE false END
            )
            FROM Follow f
            JOIN f.followee u
            LEFT JOIN Follow cf ON cf.id.followerId = :currentUserId AND cf.id.followeeId = u.id AND cf.isValid = true
            LEFT JOIN Follow cb ON cb.id.followerId = u.id AND cb.id.followeeId = :currentUserId AND cb.isValid = true
            WHERE f.id.followerId = :targetUserId AND f.isValid = true
            ORDER BY f.createdAt DESC
        """,
        countQuery = """
            SELECT COUNT(f)
            FROM Follow f
            WHERE f.id.followerId = :targetUserId AND f.isValid = true
        """
    )
    fun findFollowingProfilesWithStatus(targetUserId: Long, currentUserId: Long, pageable: Pageable): Page<ProfileResponse>

    @Query(
        value = """
            SELECT new com.neerajsahu.flux.server.relationship.api.dto.ProfileResponse(
                u.id,
                u._username,
                u._username,
                u.profilePicUrl,
                u.bio,
                CASE WHEN cf.id.followerId IS NOT NULL THEN true ELSE false END,
                CASE WHEN cb.id.followerId IS NOT NULL THEN true ELSE false END
            )
            FROM User u
            LEFT JOIN Follow cf ON cf.id.followerId = :currentUserId AND cf.id.followeeId = u.id AND cf.isValid = true
            LEFT JOIN Follow cb ON cb.id.followerId = u.id AND cb.id.followeeId = :currentUserId AND cb.isValid = true
            WHERE LOWER(u._username) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY u._username ASC
        """,
        countQuery = """
            SELECT COUNT(u)
            FROM User u
            WHERE LOWER(u._username) LIKE LOWER(CONCAT('%', :query, '%'))
        """
    )
    fun searchProfilesWithStatus(query: String, currentUserId: Long, pageable: Pageable): Page<ProfileResponse>

}