package com.neerajsahu.flux.androidclient.feature.relationship.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.RelationshipUser
import com.neerajsahu.flux.androidclient.feature.relationship.domain.repository.RelationshipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ExploreUiState(
    val searchQuery: String = "",
    val searchResults: List<RelationshipUser> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: RelationshipRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExploreUiState())
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        setupSearchDebounce()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        _searchQuery
            .debounce(500L) // Wait for 500ms stop typing
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    _state.update { it.copy(searchResults = emptyList(), isSearching = false, error = null) }
                } else {
                    performSearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        _state.update { it.copy(searchQuery = newQuery) }
    }

    private fun performSearch(query: String) {
        _state.update { it.copy(isSearching = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.searchUsers(query, 0, 50)) {
                is AppResult.Success -> {
                    _state.update { it.copy(searchResults = result.data, isSearching = false) }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(error = result.message, isSearching = false) }
                }
            }
        }
    }

    fun toggleFollow(userId: Long) {
        viewModelScope.launch {
            val user = _state.value.searchResults.find { it.id == userId } ?: return@launch
            val requestId = UUID.randomUUID().toString()
            
            // Optimistic Update
            val isFollowingBefore = user.isFollowing
            updateUserFollowStatus(userId, !isFollowingBefore)
            
            when (val result = repository.toggleFollow(userId, requestId)) {
                is AppResult.Success -> {
                    val isNowFollowing = result.data.status == "Followed"
                    updateUserFollowStatus(userId, isNowFollowing)
                }
                is AppResult.Error -> {
                    // Revert on error
                    updateUserFollowStatus(userId, isFollowingBefore)
                    _state.update { it.copy(error = result.message) }
                }
            }
        }
    }

    private fun updateUserFollowStatus(userId: Long, isFollowing: Boolean) {
        _state.update { currentState ->
            currentState.copy(
                searchResults = currentState.searchResults.map {
                    if (it.id == userId) it.copy(isFollowing = isFollowing) else it
                }
            )
        }
    }
}
