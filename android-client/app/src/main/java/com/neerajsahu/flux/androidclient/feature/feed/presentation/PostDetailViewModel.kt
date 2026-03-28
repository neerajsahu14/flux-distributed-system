package com.neerajsahu.flux.androidclient.feature.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.PostDetail
import com.neerajsahu.flux.androidclient.feature.feed.domain.repository.FeedRepository
import com.neerajsahu.flux.androidclient.feature.interaction.domain.model.PostInteractionState
import com.neerajsahu.flux.androidclient.feature.interaction.domain.repository.InteractionRepository
import com.neerajsahu.flux.androidclient.feature.interaction.domain.repository.InteractionSyncSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailUiState(
    val isLoading: Boolean = false,
    val post: PostDetail? = null,
    val error: String? = null,
    val isInteractionInFlight: Boolean = false
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val interactionRepository: InteractionRepository,
    private val interactionSyncSource: InteractionSyncSource
) : ViewModel() {

    private val _state = MutableStateFlow(PostDetailUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            interactionSyncSource.states.collect { shared ->
                val current = _state.value.post ?: return@collect
                val synced = current.applyShared(shared[current.id])
                if (synced != current) {
                    _state.value = _state.value.copy(post = synced)
                }
            }
        }
    }

    fun loadPostDetail(postId: Long) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            when (val result = repository.getPostDetail(postId)) {
                is AppResult.Success -> {
                    val sharedSnapshot = interactionSyncSource.snapshot()
                    val syncedPost = result.data.applyShared(sharedSnapshot[result.data.id])
                    _state.value = _state.value.copy(
                        isLoading = false,
                        post = syncedPost,
                        error = null
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }

    fun retry(postId: Long) {
        loadPostDetail(postId)
    }

    fun onLikeClick() {
        val post = _state.value.post ?: return
        if (_state.value.isInteractionInFlight) return

        val shouldLike = !post.isLiked
        val delta = if (shouldLike) 1 else -1
        mutatePost { it.copy(isLiked = shouldLike, likeCount = (it.likeCount + delta).coerceAtLeast(0)) }
        publishSharedState()

        withInteraction {
            when (val result = if (shouldLike) interactionRepository.likePost(post.id) else interactionRepository.unlikePost(post.id)) {
                is AppResult.Success -> _state.value = _state.value.copy(error = null)
                is AppResult.Error -> {
                    mutatePost { it.copy(isLiked = !shouldLike, likeCount = (it.likeCount - delta).coerceAtLeast(0)) }
                    publishSharedState()
                    _state.value = _state.value.copy(error = result.message)
                }
            }
            publishSharedState()
        }
    }

    fun onBookmarkClick() {
        val post = _state.value.post ?: return
        if (_state.value.isInteractionInFlight) return

        val shouldBookmark = !post.isBookmarked
        mutatePost { it.copy(isBookmarked = shouldBookmark) }
        publishSharedState()

        withInteraction {
            when (val result = if (shouldBookmark) interactionRepository.bookmarkPost(post.id) else interactionRepository.unbookmarkPost(post.id)) {
                is AppResult.Success -> _state.value = _state.value.copy(error = null)
                is AppResult.Error -> {
                    mutatePost { it.copy(isBookmarked = !shouldBookmark) }
                    publishSharedState()
                    _state.value = _state.value.copy(error = result.message)
                }
            }
            publishSharedState()
        }
    }

    fun onShareClick() {
        val post = _state.value.post ?: return
        if (_state.value.isInteractionInFlight) return

        mutatePost { it.copy(shareCount = it.shareCount + 1) }
        publishSharedState()

        withInteraction {
            when (val result = interactionRepository.sharePost(post.id)) {
                is AppResult.Success -> _state.value = _state.value.copy(error = null)
                is AppResult.Error -> {
                    mutatePost { it.copy(shareCount = (it.shareCount - 1).coerceAtLeast(0)) }
                    publishSharedState()
                    _state.value = _state.value.copy(error = result.message)
                }
            }
            publishSharedState()
        }
    }

    private inline fun mutatePost(transform: (PostDetail) -> PostDetail) {
        val current = _state.value.post ?: return
        _state.value = _state.value.copy(post = transform(current))
    }

    private inline fun withInteraction(crossinline block: suspend () -> Unit) {
        _state.value = _state.value.copy(isInteractionInFlight = true)
        viewModelScope.launch {
            block()
            _state.value = _state.value.copy(isInteractionInFlight = false)
        }
    }

    private fun publishSharedState() {
        val post = _state.value.post ?: return
        viewModelScope.launch {
            interactionSyncSource.upsert(
                PostInteractionState(
                    postId = post.id,
                    isLiked = post.isLiked,
                    isBookmarked = post.isBookmarked,
                    likeCount = post.likeCount,
                    shareCount = post.shareCount
                )
            )
        }
    }

    private fun PostDetail.applyShared(shared: PostInteractionState?): PostDetail {
        if (shared == null) return this
        return copy(
            isLiked = shared.isLiked,
            isBookmarked = shared.isBookmarked,
            likeCount = shared.likeCount,
            shareCount = shared.shareCount
        )
    }
}

