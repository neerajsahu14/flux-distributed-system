package com.neerajsahu.flux.server.common.util

import com.neerajsahu.flux.server.common.exception.PostNotFoundException
import com.neerajsahu.flux.server.feed.api.dto.PostResponse
import com.neerajsahu.flux.server.feed.domain.model.Post
import com.neerajsahu.flux.server.feed.domain.repository.PostRepository
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

/**
 * Utility class for common post-related operations.
 * Centralizes repeated patterns like fetching posts with attachments.
 */
@Component
class PostUtils(
    private val postRepository: PostRepository
) {

    /**
     * Fetches a valid post by ID, throws an exception if not found.
     */
    fun getValidPostOrThrow(postId: Long): Post {
        return postRepository.findByPostId(postId)
            ?: throw PostNotFoundException(postId)
    }

    /**
     * Fetches attachments for a page of posts and returns a map for a quick lookup.
     * This avoids N+1 query issues by fetching attachments in a single query.
     *
     * @param postsPage The page of posts (without attachments loaded)
     * @return Map of postId to Post (with attachments loaded)
     */
    fun fetchAttachmentsForPosts(postsPage: Page<Post>): Map<Long?, Post> {
        val postIds = postsPage.content.mapNotNull { it.id }
        if (postIds.isEmpty()) return emptyMap()

        val postsWithAttachments = postRepository.findPostsWithAttachmentsByIds(postIds)
        return postsWithAttachments.associateBy { it.id }
    }

    /**
     * Fetches attachments for a list of posts and returns a map for a quick lookup.
     *
     * @param posts The list of posts (without attachments loaded)
     * @return Map of postId to Post (with attachments loaded)
     */
    fun fetchAttachmentsForPosts(posts: List<Post>): Map<Long?, Post> {
        val postIds = posts.mapNotNull { it.id }
        if (postIds.isEmpty()) return emptyMap()

        val postsWithAttachments = postRepository.findPostsWithAttachmentsByIds(postIds)
        return postsWithAttachments.associateBy { it.id }
    }

    /**
     * Maps a page of posts to PostResponse list, fetching attachments efficiently.
     *
     * @param postsPage The page of posts
     * @param mapper Function to convert Post to PostResponse
     * @return List of PostResponse DTOs
     */
    fun mapPageToPostResponses(
        postsPage: Page<Post>,
        mapper: (Post) -> PostResponse
    ): List<PostResponse> {
        if (postsPage.content.isEmpty()) return emptyList()

        val postsMap = fetchAttachmentsForPosts(postsPage)

        return postsPage.content.map { post ->
            mapper(postsMap[post.id] ?: post)
        }
    }
}


