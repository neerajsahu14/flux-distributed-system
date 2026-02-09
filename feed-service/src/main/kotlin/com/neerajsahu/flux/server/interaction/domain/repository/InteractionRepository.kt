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

    @Query("""
        SELECT i FROM Interaction i 
        WHERE i.user.id = :userId 
        AND i.post.id = :postId 
        AND i.actionType = :actionType 
        AND i.isValid = true
    """)
    fun findByUserIdAndPostIdAndActionType(userId: Long, postId: Long, actionType: ActionType): Interaction?

    @Query("""
        SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END 
        FROM Interaction i 
        WHERE i.user.id = :userId 
        AND i.post.id = :postId 
        AND i.actionType = :actionType 
        AND i.isValid = true
    """)
    fun existsByUserIdAndPostIdAndActionType(userId: Long, postId: Long, actionType: ActionType): Boolean

    // Join from Interaction to Post with stable ordering by interaction createdAt
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

    @Query("SELECT COUNT(i) FROM Interaction i WHERE i.post.id = :postId AND i.actionType = :actionType AND i.isValid = true")
    fun countByPostIdAndActionType(postId: Long, actionType: ActionType): Long
}

