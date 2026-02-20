package com.neerajsahu.flux.server.interaction.service

import com.neerajsahu.flux.server.auth.domain.model.User
import com.neerajsahu.flux.server.feed.domain.model.Post
import com.neerajsahu.flux.server.interaction.domain.model.ActionType
import com.neerajsahu.flux.server.interaction.domain.model.Interaction
import com.neerajsahu.flux.server.interaction.domain.repository.InteractionRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Result of an atomic interaction operation.
 */
sealed class InteractionResult {
    data class Created(val interaction: Interaction) : InteractionResult()
    data object AlreadyExists : InteractionResult()
    data class Error(val exception: Exception) : InteractionResult()
}

/**
 * Helper service for atomic interaction operations.
 * Uses REQUIRES_NEW transaction propagation to isolate constraint violations
 * from the main transaction, preventing rollback-only state.
 */
@Service
class InteractionHelper(
    private val interactionRepository: InteractionRepository
) {
    private val logger = LoggerFactory.getLogger(InteractionHelper::class.java)

    /**
     * Attempts to create an interaction atomically.
     * If a duplicate exists (unique constraint violation), returns AlreadyExists instead of throwing.
     *
     * Uses REQUIRES_NEW to isolate this operation in a separate transaction,
     * so constraint violations don't mark the outer transaction as rollback-only.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun tryCreateInteraction(user: User, post: Post, actionType: ActionType): InteractionResult {
        return try {
            val interaction = Interaction(
                user = user,
                post = post,
                actionType = actionType,
                requestId = UUID.randomUUID().toString() // Generate unique request ID
            )
            val saved = interactionRepository.save(interaction)
            // Force flush to trigger constraint check immediately
            interactionRepository.flush()
            InteractionResult.Created(saved)
        } catch (e: DataIntegrityViolationException) {
            logger.debug("Interaction already exists: user={}, post={}, type={}", user.id, post.id, actionType)
            InteractionResult.AlreadyExists
        }
    }
}