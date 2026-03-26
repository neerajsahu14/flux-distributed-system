package com.neerajsahu.flux.androidclient.feature.relationship.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.ProfileStats
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.RelationshipUser
import com.neerajsahu.flux.androidclient.feature.relationship.domain.repository.RelationshipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: RelationshipRepository
) : ViewModel() {

    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    fun getProfile(userId: Long) {
        _state.value = _state.value.copy(
            isLoading = _state.value.profile == null,
            error = null
        )

        repository.getProfileStats(userId).onEach { result ->
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
    }

    fun getFollowers(userId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = repository.getFollowers(userId, 0, 100)) {
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
        }
    }

    fun getFollowing(userId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = repository.getFollowing(userId, 0, 100)) {
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
        }
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
