package com.neerajsahu.flux.server.interaction.domain.repository

import com.neerajsahu.flux.server.feed.domain.model.Post
import com.neerajsahu.flux.server.interaction.domain.model.ActionType
import com.neerajsahu.flux.server.interaction.domain.model.Interaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface InteractionRepository : JpaRepository<Interaction, Long> {

    interface PostInteractionFlagsProjection {
        val postId: Long
        val likedCount: Long
        val bookmarkedCount: Long
    }

    fun existsByRequestId(requestId: String): Boolean

    @Query("""
        SELECT i FROM Interaction i 
        WHERE i.user.id = :userId 
        AND i.post.id = :postId 
        AND i.actionType = :actionType
    """)
    fun findExistingInteraction(userId: Long, postId: Long, actionType: ActionType): Interaction?

    // 3. READ QUERIES (UI ke liye)
    @Query("""
        SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END 
        FROM Interaction i 
        WHERE i.user.id = :userId 
        AND i.post.id = :postId 
        AND i.actionType = :actionType 
        AND i.isValid = true
    """)
    fun existsByUserIdAndPostIdAndActionType(userId: Long, postId: Long, actionType: ActionType): Boolean

    @Query(
        """
        SELECT i.post.id AS postId,
               SUM(CASE WHEN i.actionType = com.neerajsahu.flux.server.interaction.domain.model.ActionType.LIKED AND i.isValid = true THEN 1 ELSE 0 END) AS likedCount,
               SUM(CASE WHEN i.actionType = com.neerajsahu.flux.server.interaction.domain.model.ActionType.BOOKMARKED AND i.isValid = true THEN 1 ELSE 0 END) AS bookmarkedCount
        FROM Interaction i
        WHERE i.user.id = :userId
          AND i.post.id IN :postIds
        GROUP BY i.post.id
        """
    )
    fun findInteractionFlagsByUserIdAndPostIds(
        userId: Long,
        postIds: List<Long>
    ): List<PostInteractionFlagsProjection>

    @Query(
        value = """
            SELECT p FROM Interaction i 
            JOIN i.post p
            JOIN FETCH p.author
            WHERE i.user.id = :userId 
            AND i.actionType = :actionType 
            AND i.isValid = true
            AND p.isValid = true
            ORDER BY i.createdAt DESC
        """,
        countQuery = """
            SELECT COUNT(i) FROM Interaction i 
            WHERE i.user.id = :userId 
            AND i.actionType = :actionType 
            AND i.isValid = true
            AND i.post.isValid = true
        """
    )
    fun findPostsByUserIdAndActionType(userId: Long, actionType: ActionType, pageable: Pageable): Page<Post>
}