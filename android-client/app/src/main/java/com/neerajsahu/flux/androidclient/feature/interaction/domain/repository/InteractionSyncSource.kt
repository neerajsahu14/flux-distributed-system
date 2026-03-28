package com.neerajsahu.flux.androidclient.feature.interaction.domain.repository

import com.neerajsahu.flux.androidclient.feature.interaction.domain.model.PostInteractionState
import kotlinx.coroutines.flow.Flow

interface InteractionSyncSource {
    val states: Flow<Map<Long, PostInteractionState>>

    suspend fun upsert(state: PostInteractionState)
    suspend fun snapshot(): Map<Long, PostInteractionState>
}


