package com.neerajsahu.flux.server.common.mapper

import com.neerajsahu.flux.server.auth.api.dto.UserResponse
import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.feed.api.dto.AttachmentResponse
import com.neerajsahu.flux.server.feed.api.dto.PostDetailResponse
import com.neerajsahu.flux.server.feed.api.dto.PostResponse
import com.neerajsahu.flux.server.feed.domain.model.Post
import com.neerajsahu.flux.server.feed.domain.model.PostAttachment
import org.springframework.stereotype.Component

/**
 * Centralized mapper for converting domain entities to DTOs.
 */
@Component
class PostMapper {

    /**
     * Maps a Post entity to PostResponse DTO
     */
    fun toPostResponse(post: Post, userMapper: (User) -> UserResponse): PostResponse {
        val attachment = getFirstValidAttachment(post)
        val displayImageUrl = attachment?.thumbnailUrl ?: attachment?.contentUrl ?: ""

        return PostResponse(
            id = post.id!!,
            caption = post.content,
            imageUrl = displayImageUrl,
            author = userMapper(post.author),
            createdAt = post.createdAt.toString(),
            likeCount = post.likeCount
        )
    }

    /**
     * Maps a Post entity to PostDetailResponse DTO with interaction status
     */
    fun toPostDetailResponse(
        post: Post,
        userMapper: (User) -> UserResponse,
        isLiked: Boolean,
        isBookmarked: Boolean
    ): PostDetailResponse {
        val validAttachments = getValidAttachments(post)
        val attachment = validAttachments.firstOrNull()
        val displayImageUrl = attachment?.thumbnailUrl ?: attachment?.contentUrl ?: ""

        return PostDetailResponse(
            id = post.id!!,
            caption = post.content,
            imageUrl = displayImageUrl,
            contentUrl = attachment?.contentUrl,
            mediaType = attachment?.mediaType?.name ?: "IMAGE",
            author = userMapper(post.author),
            createdAt = post.createdAt.toString(),
            likeCount = post.likeCount,
            shareCount = post.shareCount,
            isLiked = isLiked,
            isBookmarked = isBookmarked,
            attachments = validAttachments.map { toAttachmentResponse(it) }
        )
    }

    /**
     * Maps a PostAttachment entity to AttachmentResponse DTO
     */
    fun toAttachmentResponse(attachment: PostAttachment): AttachmentResponse {
        return AttachmentResponse(
            id = attachment.id!!,
            contentUrl = attachment.contentUrl,
            thumbnailUrl = attachment.thumbnailUrl,
            mediaType = attachment.mediaType.name,
            displayOrder = attachment.displayOrder
        )
    }

    /**
     * Gets valid (non-deleted) attachments from a post
     */
    fun getValidAttachments(post: Post): List<PostAttachment> {
        return post.attachments.filter { it.isValid }
    }

    /**
     * Gets the first valid attachment (for thumbnail display)
     */
    fun getFirstValidAttachment(post: Post): PostAttachment? {
        return getValidAttachments(post).firstOrNull()
    }
}

