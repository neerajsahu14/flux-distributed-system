package com.neerajsahu.flux.server.interaction.api.dto

/**
 * Response action types for interaction API.
 * Separate from domain ActionType to allow API-specific values like UNLIKED/UNBOOKMARKED.
 */
enum class ResponseActionType {
    LIKED, UNLIKED, BOOKMARKED, UNBOOKMARKED, SHARED
}

data class InteractionResponse(
    val postId: Long,
    val actionType: ResponseActionType,
    val success: Boolean,
    val message: String,
    val currentCount: Long? = null
)