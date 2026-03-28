package com.neerajsahu.flux.androidclient.feature.interaction.data.repository

import com.neerajsahu.flux.androidclient.feature.interaction.data.local.InteractionDao
import com.neerajsahu.flux.androidclient.feature.interaction.data.local.InteractionEntity
import com.neerajsahu.flux.androidclient.feature.interaction.domain.model.PostInteractionState
import com.neerajsahu.flux.androidclient.feature.interaction.domain.repository.InteractionSyncSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomInteractionSyncSource @Inject constructor(
    private val interactionDao: InteractionDao
) : InteractionSyncSource {

    override val states: Flow<Map<Long, PostInteractionState>> =
        interactionDao.observeAll().map { entities ->
            entities.associate { it.postId to it.toDomain() }
        }

    override suspend fun upsert(state: PostInteractionState) {
        interactionDao.upsert(state.toEntity())
    }

    override suspend fun snapshot(): Map<Long, PostInteractionState> {
        return interactionDao.getAll().associate { it.postId to it.toDomain() }
    }

    private fun InteractionEntity.toDomain(): PostInteractionState {
        return PostInteractionState(
            postId = postId,
            isLiked = isLiked,
            isBookmarked = isBookmarked,
            likeCount = likeCount,
            shareCount = shareCount
        )
    }

    private fun PostInteractionState.toEntity(): InteractionEntity {
        return InteractionEntity(
            postId = postId,
            isLiked = isLiked,
            isBookmarked = isBookmarked,
            likeCount = likeCount,
            shareCount = shareCount
        )
    }
}

