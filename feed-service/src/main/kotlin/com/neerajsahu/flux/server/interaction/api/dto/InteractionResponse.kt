package com.neerajsahu.flux.server.interaction.api.dto

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

data class InteractionRequest(
    val requestId: String
)