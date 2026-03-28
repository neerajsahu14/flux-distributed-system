package com.neerajsahu.flux.server.interaction.service

import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.auth.service.AuthService
import com.neerajsahu.flux.server.common.mapper.PostMapper
import com.neerajsahu.flux.server.common.util.PostUtils
import com.neerajsahu.flux.server.feed.api.dto.PostResponse
import com.neerajsahu.flux.server.feed.domain.model.Post
import com.neerajsahu.flux.server.feed.domain.repository.PostRepository
import com.neerajsahu.flux.server.interaction.api.dto.InteractionResponse
import com.neerajsahu.flux.server.interaction.api.dto.ResponseActionType
import com.neerajsahu.flux.server.interaction.domain.model.ActionType
import com.neerajsahu.flux.server.interaction.domain.model.Interaction
import com.neerajsahu.flux.server.interaction.domain.repository.InteractionRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InteractionService(
    private val interactionRepository: InteractionRepository,
    private val postRepository: PostRepository,
    private val authService: AuthService,
    private val postMapper: PostMapper,
    private val postUtils: PostUtils
) {
    data class PostInteractionFlags(
        val isLiked: Boolean,
        val isBookmarked: Boolean
    )

    @Transactional
    fun likePost(user: User, postId: Long, requestId: String): InteractionResponse {
        val post = postUtils.getValidPostOrThrow(postId)

        // Idempotency: Ignore duplicate requests from client retry
        if (interactionRepository.existsByRequestId(requestId)) {
            return InteractionResponse(postId, ResponseActionType.LIKED, true, "Already processed", post.likeCount.toLong())
        }

        val existing = interactionRepository.findExistingInteraction(user.id!!, postId, ActionType.LIKED)

        if (existing != null) {
            if (!existing.isValid) {
                existing.isValid = true
                existing.requestId = requestId // Bind new requestId to the toggle action
                interactionRepository.save(existing)

                post.likeCount += 1
                postRepository.save(post)
            }
        } else {
            val newLike = Interaction(user = user, post = post, actionType = ActionType.LIKED, requestId = requestId, isValid = true)
            interactionRepository.save(newLike)

            post.likeCount += 1
            postRepository.save(post)
        }

        return InteractionResponse(postId, ResponseActionType.LIKED, true, "Post liked successfully", post.likeCount.toLong())
    }

    @Transactional
    fun unlikePost(user: User, postId: Long, requestId: String): InteractionResponse {
        val post = postUtils.getValidPostOrThrow(postId)

        if (interactionRepository.existsByRequestId(requestId)) {
            return InteractionResponse(postId, ResponseActionType.UNLIKED, true, "Already processed", post.likeCount.toLong())
        }

        val existing = interactionRepository.findExistingInteraction(user.id!!, postId, ActionType.LIKED)

        if (existing != null && existing.isValid) {
            existing.isValid = false // Soft delete
            existing.requestId = requestId
            interactionRepository.save(existing)

            post.likeCount = maxOf(0, post.likeCount - 1)
            postRepository.save(post)
        }

        return InteractionResponse(postId, ResponseActionType.UNLIKED, true, "Post unliked successfully", post.likeCount.toLong())
    }

    @Transactional
    fun bookmarkPost(user: User, postId: Long, requestId: String): InteractionResponse {
        val post = postUtils.getValidPostOrThrow(postId)

        if (interactionRepository.existsByRequestId(requestId)) {
            return InteractionResponse(postId, ResponseActionType.BOOKMARKED, true, "Already processed", 0)
        }

        val existing = interactionRepository.findExistingInteraction(user.id!!, postId, ActionType.BOOKMARKED)

        if (existing != null) {
            if (!existing.isValid) {
                existing.isValid = true
                existing.requestId = requestId
                interactionRepository.save(existing)
            }
        } else {
            val newBookmark = Interaction(user = user, post = post, actionType = ActionType.BOOKMARKED, requestId = requestId, isValid = true)
            interactionRepository.save(newBookmark)
        }

        return InteractionResponse(postId, ResponseActionType.BOOKMARKED, true, "Post bookmarked successfully", 0)
    }

    @Transactional
    fun unbookmarkPost(user: User, postId: Long, requestId: String): InteractionResponse {
        postUtils.getValidPostOrThrow(postId)

        if (interactionRepository.existsByRequestId(requestId)) {
            return InteractionResponse(postId, ResponseActionType.UNBOOKMARKED, true, "Already processed", 0)
        }

        val existing = interactionRepository.findExistingInteraction(user.id!!, postId, ActionType.BOOKMARKED)

        if (existing != null && existing.isValid) {
            existing.isValid = false
            existing.requestId = requestId
            interactionRepository.save(existing)
        }

        return InteractionResponse(postId, ResponseActionType.UNBOOKMARKED, true, "Bookmark removed successfully", 0)
    }

    @Transactional
    fun sharePost(user: User, postId: Long, requestId: String): InteractionResponse {
        val post = postUtils.getValidPostOrThrow(postId)

        if (interactionRepository.existsByRequestId(requestId)) {
            return InteractionResponse(postId, ResponseActionType.SHARED, true, "Already processed", post.shareCount.toLong())
        }

        val newShare = Interaction(user = user, post = post, actionType = ActionType.SHARED, requestId = requestId, isValid = true)
        interactionRepository.save(newShare)

        post.shareCount += 1
        postRepository.save(post)

        return InteractionResponse(postId, ResponseActionType.SHARED, true, "Post shared successfully", post.shareCount.toLong())
    }

    @Transactional(readOnly = true)
    fun getBookmarkedPosts(user: User, page: Int, size: Int): List<PostResponse> {
        val pageable = PageRequest.of(page, size)
        val postsPage = interactionRepository.findPostsByUserIdAndActionType(user.id!!, ActionType.BOOKMARKED, pageable)
        return mapPostsWithInteractionFlags(postsPage.content, user.id!!)
    }

    @Transactional(readOnly = true)
    fun getLikedPosts(user: User, page: Int, size: Int): List<PostResponse> {
        val pageable = PageRequest.of(page, size)
        val postsPage = interactionRepository.findPostsByUserIdAndActionType(user.id!!, ActionType.LIKED, pageable)
        return mapPostsWithInteractionFlags(postsPage.content, user.id!!)
    }

    fun getInteractionFlagsForPosts(userId: Long, postIds: List<Long>): Map<Long, PostInteractionFlags> {
        if (postIds.isEmpty()) return emptyMap()

        return interactionRepository.findInteractionFlagsByUserIdAndPostIds(userId, postIds)
            .associate { projection ->
                projection.postId to PostInteractionFlags(
                    isLiked = projection.likedCount > 0,
                    isBookmarked = projection.bookmarkedCount > 0
                )
            }
    }

    fun isPostLikedByUser(userId: Long, postId: Long): Boolean =
        interactionRepository.existsByUserIdAndPostIdAndActionType(userId, postId, ActionType.LIKED)

    fun isPostBookmarkedByUser(userId: Long, postId: Long): Boolean =
        interactionRepository.existsByUserIdAndPostIdAndActionType(userId, postId, ActionType.BOOKMARKED)

    private fun mapPostsWithInteractionFlags(posts: List<Post>, userId: Long): List<PostResponse> {
        if (posts.isEmpty()) return emptyList()

        val postsById = postUtils.fetchAttachmentsForPosts(posts)
        val enrichedPosts = posts.map { postsById[it.id] ?: it }
        val postIds = enrichedPosts.mapNotNull { it.id }
        val flagsByPostId = getInteractionFlagsForPosts(userId, postIds)

        return enrichedPosts.map { post ->
            val flags = flagsByPostId[post.id] ?: PostInteractionFlags(false, false)
            postMapper.toPostResponse(
                post = post,
                userMapper = { authService.getUserResponse(it) },
                isLiked = flags.isLiked,
                isBookmarked = flags.isBookmarked
            )
        }
    }
}