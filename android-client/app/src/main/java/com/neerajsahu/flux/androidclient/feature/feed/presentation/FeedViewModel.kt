package com.neerajsahu.flux.androidclient.feature.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.Post
import com.neerajsahu.flux.androidclient.feature.feed.domain.repository.FeedRepository
import com.neerajsahu.flux.androidclient.feature.interaction.domain.model.PostInteractionState
import com.neerajsahu.flux.androidclient.feature.interaction.domain.repository.InteractionRepository
import com.neerajsahu.flux.androidclient.feature.interaction.domain.repository.InteractionSyncSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FeedType {
    GLOBAL, TIMELINE
}

data class FeedUiState(
    val isLoading: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
    val isLoadingMore: Boolean = false,
    val interactionInFlightPostIds: Set<Long> = emptySet()
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val interactionRepository: InteractionRepository,
    private val interactionSyncSource: InteractionSyncSource
) : ViewModel() {

    private val _state = MutableStateFlow(FeedUiState())
    val state = _state.asStateFlow()

    private var feedJob: Job? = null
    private var currentFeedType = FeedType.GLOBAL

    init {
        viewModelScope.launch {
            interactionSyncSource.states.collect { shared ->
                if (shared.isEmpty() || _state.value.posts.isEmpty()) return@collect
                _state.value = _state.value.copy(
                    posts = _state.value.posts.map { post ->
                        val updated = shared[post.id]
                        if (updated == null) post else post.copy(
                            isLiked = updated.isLiked,
                            isBookmarked = updated.isBookmarked,
                            likeCount = updated.likeCount,
                            shareCount = updated.shareCount
                        )
                    }
                )
            }
        }
    }

    fun loadGlobalFeed(page: Int = 0, size: Int = 20) {
        currentFeedType = FeedType.GLOBAL
        loadFeed(page, size) { repository.getGlobalFeed(page, size) }
    }

    fun loadTimelineFeed(page: Int = 0, size: Int = 20) {
        currentFeedType = FeedType.TIMELINE
        loadFeed(page, size) { repository.getTimelineFeed(page, size) }
    }

    fun loadMorePosts(size: Int = 20) {
        val nextPage = _state.value.currentPage + 1
        _state.value = _state.value.copy(isLoadingMore = true)

        feedJob?.cancel()
        feedJob = viewModelScope.launch {
            val loadMore = when (currentFeedType) {
                FeedType.GLOBAL -> { { repository.getGlobalFeed(nextPage, size) } }
                FeedType.TIMELINE -> { { repository.getTimelineFeed(nextPage, size) } }
            }

            loadMore().collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        val sharedSnapshot = interactionSyncSource.snapshot()
                        val syncedIncoming = result.data.map { it.applyShared(sharedSnapshot[it.id]) }
                        val newPosts = if (result.data.isEmpty()) {
                            _state.value.posts
                        } else {
                            _state.value.posts + syncedIncoming
                        }
                        _state.value = _state.value.copy(
                            isLoadingMore = false,
                            posts = newPosts,
                            currentPage = nextPage,
                            hasMore = syncedIncoming.isNotEmpty(),
                            error = null
                        )
                    }
                    is AppResult.Error -> {
                        _state.value = _state.value.copy(
                            isLoadingMore = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        when (currentFeedType) {
            FeedType.GLOBAL -> loadGlobalFeed()
            FeedType.TIMELINE -> loadTimelineFeed()
        }
    }

    fun onLikeClick(postId: Long) {
        val post = _state.value.posts.firstOrNull { it.id == postId } ?: return
        if (_state.value.interactionInFlightPostIds.contains(postId)) return

        val shouldLike = !post.isLiked
        val delta = if (shouldLike) 1 else -1

        mutatePost(postId) {
            it.copy(
                isLiked = shouldLike,
                likeCount = (it.likeCount + delta).coerceAtLeast(0)
            )
        }
        publishSharedState(postId)
        markInteractionInFlight(postId, true)

        viewModelScope.launch {
            val result = if (shouldLike) interactionRepository.likePost(postId) else interactionRepository.unlikePost(postId)
            when (result) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(error = null)
                }
                is AppResult.Error -> {
                    // Roll back optimistic state if request fails.
                    mutatePost(postId) {
                        it.copy(
                            isLiked = !shouldLike,
                            likeCount = (it.likeCount - delta).coerceAtLeast(0)
                        )
                    }
                    publishSharedState(postId)
                    _state.value = _state.value.copy(error = result.message)
                }
            }
            publishSharedState(postId)
            markInteractionInFlight(postId, false)
        }
    }

    fun onBookmarkClick(postId: Long) {
        val post = _state.value.posts.firstOrNull { it.id == postId } ?: return
        if (_state.value.interactionInFlightPostIds.contains(postId)) return

        val shouldBookmark = !post.isBookmarked
        mutatePost(postId) { it.copy(isBookmarked = shouldBookmark) }
        publishSharedState(postId)
        markInteractionInFlight(postId, true)

        viewModelScope.launch {
            val result = if (shouldBookmark) interactionRepository.bookmarkPost(postId) else interactionRepository.unbookmarkPost(postId)
            when (result) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(error = null)
                }
                is AppResult.Error -> {
                    mutatePost(postId) { it.copy(isBookmarked = !shouldBookmark) }
                    publishSharedState(postId)
                    _state.value = _state.value.copy(error = result.message)
                }
            }
            publishSharedState(postId)
            markInteractionInFlight(postId, false)
        }
    }

    fun onShareClick(postId: Long) {
        if (_state.value.interactionInFlightPostIds.contains(postId)) return

        mutatePost(postId) { it.copy(shareCount = it.shareCount + 1) }
        publishSharedState(postId)
        markInteractionInFlight(postId, true)

        viewModelScope.launch {
            when (val result = interactionRepository.sharePost(postId)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(error = null)
                }
                is AppResult.Error -> {
                    mutatePost(postId) { it.copy(shareCount = (it.shareCount - 1).coerceAtLeast(0)) }
                    publishSharedState(postId)
                    _state.value = _state.value.copy(error = result.message)
                }
            }
            publishSharedState(postId)
            markInteractionInFlight(postId, false)
        }
    }

    private fun loadFeed(page: Int, size: Int, networkCall: suspend () -> kotlinx.coroutines.flow.Flow<AppResult<List<Post>>>) {
        feedJob?.cancel()
        feedJob = viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = _state.value.posts.isEmpty(),
                error = null,
                currentPage = page
            )

            networkCall().collect { result ->
                when (result) {
                    is AppResult.Success -> {
                        val sharedSnapshot = interactionSyncSource.snapshot()
                        val syncedPosts = result.data.map { it.applyShared(sharedSnapshot[it.id]) }
                        _state.value = _state.value.copy(
                            isLoading = false,
                            posts = syncedPosts,
                            error = null,
                            hasMore = syncedPosts.isNotEmpty()
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
    }

    private fun mutatePost(postId: Long, transform: (Post) -> Post) {
        _state.value = _state.value.copy(
            posts = _state.value.posts.map { post ->
                if (post.id == postId) transform(post) else post
            }
        )
    }

    private fun markInteractionInFlight(postId: Long, inFlight: Boolean) {
        val updated = _state.value.interactionInFlightPostIds.toMutableSet()
        if (inFlight) updated.add(postId) else updated.remove(postId)
        _state.value = _state.value.copy(interactionInFlightPostIds = updated)
    }

    private fun publishSharedState(postId: Long) {
        val post = _state.value.posts.firstOrNull { it.id == postId } ?: return
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

    private fun Post.applyShared(shared: PostInteractionState?): Post {
        if (shared == null) return this
        return copy(
            isLiked = shared.isLiked,
            isBookmarked = shared.isBookmarked,
            likeCount = shared.likeCount,
            shareCount = shared.shareCount
        )
    }
}
