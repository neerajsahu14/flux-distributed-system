package com.neerajsahu.flux.androidclient.feature.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.Post
import com.neerajsahu.flux.androidclient.feature.feed.domain.repository.FeedRepository
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
    val isLoadingMore: Boolean = false
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedUiState())
    val state = _state.asStateFlow()

    private var feedJob: Job? = null
    private var currentFeedType = FeedType.GLOBAL

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
                        val newPosts = if (result.data.isEmpty()) {
                            _state.value.posts
                        } else {
                            _state.value.posts + result.data
                        }
                        _state.value = _state.value.copy(
                            isLoadingMore = false,
                            posts = newPosts,
                            currentPage = nextPage,
                            hasMore = result.data.isNotEmpty(),
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
                        _state.value = _state.value.copy(
                            isLoading = false,
                            posts = result.data,
                            error = null,
                            hasMore = result.data.isNotEmpty()
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
}
