package com.neerajsahu.flux.androidclient.feature.relationship.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.datastore.TokenManager
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.Post
import com.neerajsahu.flux.androidclient.feature.feed.domain.repository.FeedRepository
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.ProfileStats
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.RelationshipUser
import com.neerajsahu.flux.androidclient.feature.relationship.domain.repository.RelationshipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val profile: ProfileStats? = null,
    val followers: List<RelationshipUser> = emptyList(),
    val following: List<RelationshipUser> = emptyList(),
    val posts: List<Post> = emptyList(),
    val isPostsLoading: Boolean = false,
    val isCurrentUser: Boolean = false,
    val currentUserId: Long = 0L,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: RelationshipRepository,
    private val feedRepository: FeedRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    init {
        viewModelScope.launch {
            val userId = tokenManager.getUserId().first() ?: 0L
            _state.value = _state.value.copy(currentUserId = userId)
        }
    }

    fun getProfile(userId: Long) {
        viewModelScope.launch {
            val currentUserId = tokenManager.getUserId().first() ?: 0L
            val targetUserId = if (userId == 0L) currentUserId else userId
            
            _state.value = _state.value.copy(
                isLoading = _state.value.profile == null,
                isCurrentUser = targetUserId == currentUserId,
                error = null
            )

            repository.getProfileStats(targetUserId).onEach { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.value = _state.value.copy(
                            profile = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                    is AppResult.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                }
            }.launchIn(viewModelScope)
            
            getUserPosts(targetUserId)
        }
    }

    private fun getUserPosts(userId: Long) {
        _state.value = _state.value.copy(isPostsLoading = true)
        feedRepository.getUserFeed(userId, 0, 50).onEach { result ->
            when (result) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        posts = result.data,
                        isPostsLoading = false
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        isPostsLoading = false,
                        error = result.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getFollowers(userId: Long) {
        _state.value = _state.value.copy(isLoading = true)
        repository.getFollowers(userId, 0, 100).onEach { result ->
            when (result) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        followers = result.data,
                        isLoading = false
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getFollowing(userId: Long) {
        _state.value = _state.value.copy(isLoading = true)
        repository.getFollowing(userId, 0, 100).onEach { result ->
            when (result) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        following = result.data,
                        isLoading = false
                    )
                }
                is AppResult.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun toggleFollow(targetUserId: Long) {
        viewModelScope.launch {
            val requestId = UUID.randomUUID().toString()
            
            // Optimistic update for lists
            val previousFollowers = _state.value.followers
            val previousFollowing = _state.value.following
            
            val isCurrentlyFollowing = (_state.value.followers.find { it.id == targetUserId }?.isFollowing == true) ||
                                      (_state.value.following.find { it.id == targetUserId }?.isFollowing == true) ||
                                      (_state.value.profile?.isFollowing == true)

            updateFollowStatusInLists(targetUserId, !isCurrentlyFollowing)

            when (val result = repository.toggleFollow(targetUserId, requestId)) {
                is AppResult.Success -> {
                    val isNowFollowing = result.data.status == "Followed"
                    updateFollowStatusInLists(targetUserId, isNowFollowing)
                    
                    val currentProfile = _state.value.profile
                    if (currentProfile != null && currentProfile.userId == targetUserId) {
                        val followersCount = if (isNowFollowing) {
                            currentProfile.followersCount + 1
                        } else {
                            currentProfile.followersCount - 1
                        }
                        _state.value = _state.value.copy(
                            profile = currentProfile.copy(
                                isFollowing = isNowFollowing,
                                followersCount = followersCount
                            )
                        )
                    }
                }
                is AppResult.Error -> {
                    // Revert optimistic update
                    _state.value = _state.value.copy(
                        followers = previousFollowers,
                        following = previousFollowing
                    )
                    _state.value = _state.value.copy(error = result.message)
                }
            }
        }
    }

    private fun updateFollowStatusInLists(userId: Long, isFollowing: Boolean) {
        val updatedFollowers = _state.value.followers.map {
            if (it.id == userId) it.copy(isFollowing = isFollowing) else it
        }
        val updatedFollowing = _state.value.following.map {
            if (it.id == userId) it.copy(isFollowing = isFollowing) else it
        }
        _state.value = _state.value.copy(
            followers = updatedFollowers,
            following = updatedFollowing
        )
    }
}
