package com.neerajsahu.flux.server.feed.domain.repository

import com.neerajsahu.flux.server.feed.domain.model.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<Post, Long> {
    fun existsByRequestId(requestId: String): Boolean

    @Query("SELECT p FROM Post p WHERE p.requestId = :requestId AND p.isValid = true")
    fun findByRequestId(requestId: String): List<Post>

    // Use LEFT JOIN with ON clause (Hibernate WITH) to filter attachments without excluding posts
    @Query("""
        SELECT p FROM Post p 
        LEFT JOIN FETCH p.attachments
        LEFT JOIN FETCH p.author 
        WHERE p.id = :postId AND p.isValid = true
    """)
    fun findByPostId(postId: Long): Post?

    // For paginated queries, avoid JOIN FETCH on collections to prevent incorrect pagination
    // Added ORDER BY for stable pagination
    @Query(
        value = "SELECT DISTINCT p FROM Post p JOIN FETCH p.author WHERE p.author.id = :userId AND p.isValid = true ORDER BY p.createdAt DESC",
        countQuery = "SELECT COUNT(p) FROM Post p WHERE p.author.id = :userId AND p.isValid = true"
    )
    fun findPostsByAuthorId(userId: Long, pageable: Pageable): Page<Post>

    @Query(
        value = "SELECT DISTINCT p FROM Post p JOIN FETCH p.author WHERE p.isValid = true ORDER BY p.createdAt DESC",
        countQuery = "SELECT COUNT(p) FROM Post p WHERE p.isValid = true"
    )
    fun findAllPosts(pageable: Pageable): Page<Post>

    // Separate query to fetch attachments for a list of posts (to avoid N+1)
    // Use WITH clause to filter only valid attachments without excluding posts
    @Query("""
        SELECT DISTINCT p FROM Post p 
        LEFT JOIN FETCH p.attachments
        WHERE p.id IN :postIds AND p.isValid = true
    """)
    fun findPostsWithAttachmentsByIds(postIds: List<Long>): List<Post>

}