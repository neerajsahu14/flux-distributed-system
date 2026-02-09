package com.neerajsahu.flux.server.interaction.service

import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.auth.service.AuthService
import com.neerajsahu.flux.server.common.mapper.PostMapper
import com.neerajsahu.flux.server.common.util.PostUtils
import com.neerajsahu.flux.server.feed.api.dto.PostResponse
import com.neerajsahu.flux.server.feed.domain.repository.PostRepository
import com.neerajsahu.flux.server.interaction.api.dto.InteractionResponse
import com.neerajsahu.flux.server.interaction.api.dto.ResponseActionType
import com.neerajsahu.flux.server.interaction.domain.model.ActionType
import com.neerajsahu.flux.server.interaction.domain.repository.InteractionRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InteractionService(
    private val interactionRepository: InteractionRepository,
    private val postRepository: PostRepository,
    private val authService: AuthService,
    private val postMapper: PostMapper,
    private val postUtils: PostUtils,
    private val interactionHelper: InteractionHelper
) {
    private val logger = LoggerFactory.getLogger(InteractionService::class.java)

    // ==================== LIKE / UNLIKE ====================
    @Transactional
    fun likePost(user: User, postId: Long): InteractionResponse {
        val post = postUtils.getValidPostOrThrow(postId)

        // Use atomic helper to handle race condition
        return when (val result = interactionHelper.tryCreateInteraction(user, post, ActionType.LIKED)) {
            is InteractionResult.Created -> {
                // Update like count
                post.likeCount += 1
                postRepository.save(post)

                InteractionResponse(
                    postId = postId,
                    actionType = ResponseActionType.LIKED,
                    success = true,
                    message = "Post liked successfully",
                    currentCount = post.likeCount.toLong()
                )
            }
            is InteractionResult.AlreadyExists -> {
                InteractionResponse(
                    postId = postId,
                    actionType = ResponseActionType.LIKED,
                    success = false,
                    message = "Already liked",
                    currentCount = post.likeCount.toLong()
                )
            }
            is InteractionResult.Error -> {
                throw result.exception
            }
        }
    }

    @Transactional
    fun unlikePost(user: User, postId: Long): InteractionResponse {
        val post = postUtils.getValidPostOrThrow(postId)

        val deleted = interactionHelper.tryDeleteInteraction(user.id!!, postId, ActionType.LIKED)

        return if (deleted) {
            // Update like count
            post.likeCount = maxOf(0, post.likeCount - 1)
            postRepository.save(post)

            InteractionResponse(
                postId = postId,
                actionType = ResponseActionType.UNLIKED,
                success = true,
                message = "Post unliked successfully",
                currentCount = post.likeCount.toLong()
            )
        } else {
            InteractionResponse(
                postId = postId,
                actionType = ResponseActionType.LIKED,
                success = false,
                message = "Post was not liked",
                currentCount = post.likeCount.toLong()
            )
        }
    }

    // ==================== BOOKMARK / UNBOOKMARK ====================
    @Transactional
    fun bookmarkPost(user: User, postId: Long): InteractionResponse {
        val post = postUtils.getValidPostOrThrow(postId)

        // Use atomic helper to handle race condition
        return when (val result = interactionHelper.tryCreateInteraction(user, post, ActionType.BOOKMARKED)) {
            is InteractionResult.Created -> {
                InteractionResponse(
                    postId = postId,
                    actionType = ResponseActionType.BOOKMARKED,
                    success = true,
                    message = "Post bookmarked successfully"
                )
            }
            is InteractionResult.AlreadyExists -> {
                InteractionResponse(
                    postId = postId,
                    actionType = ResponseActionType.BOOKMARKED,
                    success = false,
                    message = "Already bookmarked"
                )
            }
            is InteractionResult.Error -> {
                throw result.exception
            }
        }
    }

    @Transactional
    fun unbookmarkPost(user: User, postId: Long): InteractionResponse {
        // Validate post exists
        postUtils.getValidPostOrThrow(postId)

        val deleted = interactionHelper.tryDeleteInteraction(user.id!!, postId, ActionType.BOOKMARKED)

        return if (deleted) {
            InteractionResponse(
                postId = postId,
                actionType = ResponseActionType.UNBOOKMARKED,
                success = true,
                message = "Bookmark removed successfully"
            )
        } else {
            InteractionResponse(
                postId = postId,
                actionType = ResponseActionType.BOOKMARKED,
                success = false,
                message = "Post was not bookmarked"
            )
        }
    }

    // ==================== SHARE ====================
    @Transactional
    fun sharePost(user: User, postId: Long): InteractionResponse {
        val post = postUtils.getValidPostOrThrow(postId)

        // Try to create share interaction (may already exist for this user)
        val result = interactionHelper.tryCreateInteraction(user, post, ActionType.SHARED)

        // Always increment share count (even if user already shared before)
        // This allows counting multiple shares from same user over time
        post.shareCount += 1
        postRepository.save(post)

        if (result is InteractionResult.AlreadyExists) {
            logger.debug("User ${user.id} already shared post $postId, incrementing count anyway")
        }

        return InteractionResponse(
            postId = postId,
            actionType = ResponseActionType.SHARED,
            success = true,
            message = "Post shared successfully",
            currentCount = post.shareCount.toLong()
        )
    }

    // ==================== GET BOOKMARKED POSTS ====================
    @Transactional(readOnly = true)
    fun getBookmarkedPosts(user: User, page: Int, size: Int): List<PostResponse> {
        val pageable = PageRequest.of(page, size)
        val postsPage = interactionRepository.findPostsByUserIdAndActionType(
            user.id!!, ActionType.BOOKMARKED, pageable
        )
        return postUtils.mapPageToPostResponses(postsPage) { post ->
            postMapper.toPostResponse(post) { authService.getUserResponse(it) }
        }
    }

    // ==================== GET LIKED POSTS ====================
    @Transactional(readOnly = true)
    fun getLikedPosts(user: User, page: Int, size: Int): List<PostResponse> {
        val pageable = PageRequest.of(page, size)
        val postsPage = interactionRepository.findPostsByUserIdAndActionType(
            user.id!!, ActionType.LIKED, pageable
        )
        return postUtils.mapPageToPostResponses(postsPage) { post ->
            postMapper.toPostResponse(post) { authService.getUserResponse(it) }
        }
    }

    // ==================== CHECK INTERACTION STATUS ====================
    fun isPostLikedByUser(userId: Long, postId: Long): Boolean {
        return interactionRepository.existsByUserIdAndPostIdAndActionType(userId, postId, ActionType.LIKED)
    }

    fun isPostBookmarkedByUser(userId: Long, postId: Long): Boolean {
        return interactionRepository.existsByUserIdAndPostIdAndActionType(userId, postId, ActionType.BOOKMARKED)
    }
}