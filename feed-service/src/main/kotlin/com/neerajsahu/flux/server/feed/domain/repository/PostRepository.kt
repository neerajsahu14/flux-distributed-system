package com.neerajsahu.flux.server.feed.domain.repository

import com.neerajsahu.flux.server.feed.domain.model.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PostRepository : JpaRepository<Post, Long> {
    fun existsByRequestId(requestId: String): Boolean

    @Query("SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.attachments ORDER BY p.createdAt DESC")
    fun findAllPosts(pageable: Pageable): Page<Post>
}