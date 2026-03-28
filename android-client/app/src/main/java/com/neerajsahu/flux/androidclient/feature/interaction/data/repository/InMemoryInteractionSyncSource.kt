package com.neerajsahu.flux.androidclient.feature.interaction.data.repository

import com.neerajsahu.flux.androidclient.feature.interaction.domain.model.PostInteractionState
import com.neerajsahu.flux.androidclient.feature.interaction.domain.repository.InteractionSyncSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryInteractionSyncSource : InteractionSyncSource {
    private val _states = MutableStateFlow<Map<Long, PostInteractionState>>(emptyMap())
    override val states: Flow<Map<Long, PostInteractionState>> = _states

    override suspend fun upsert(state: PostInteractionState) {
        _states.value = _states.value + (state.postId to state)
    }

    override suspend fun snapshot(): Map<Long, PostInteractionState> = _states.value
}


