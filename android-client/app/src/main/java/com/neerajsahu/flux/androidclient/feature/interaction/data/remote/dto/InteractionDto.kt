package com.neerajsahu.flux.androidclient.feature.interaction.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.neerajsahu.flux.androidclient.feature.interaction.domain.repository.InteractionActionResult

data class InteractionRequestDto(
    @SerializedName("requestId") val requestId: String
)

data class InteractionResponseDto(
    @SerializedName("postId") val postId: Long,
    @SerializedName("actionType") val actionType: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("currentCount") val currentCount: Long?
)

fun InteractionResponseDto.toDomain(): InteractionActionResult {
    return InteractionActionResult(
        postId = postId,
        actionType = actionType,
        message = message,
        currentCount = currentCount
    )
}

