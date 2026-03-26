package com.neerajsahu.flux.androidclient.feature.relationship.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.ProfileStats
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
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: RelationshipRepository
) : ViewModel() {

    private val _state = mutableStateOf(ProfileState())
    val state: State<ProfileState> = _state

    fun getProfile(userId: Long) {
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

    fun toggleFollow(targetUserId: Long) {
        viewModelScope.launch {
            val requestId = UUID.randomUUID().toString()
            when (val result = repository.toggleFollow(targetUserId, requestId)) {
                is AppResult.Success -> {
                    val currentProfile = _state.value.profile
                    if (currentProfile != null) {
                        val isFollowing = result.data.status == "Followed"
                        val followersCount = if (isFollowing) {
                            currentProfile.followersCount + 1
                        } else {
                            currentProfile.followersCount - 1
                        }
                        _state.value = _state.value.copy(
                            profile = currentProfile.copy(
                                isFollowing = isFollowing,
                                followersCount = followersCount
                            )
                        )
                    }
                }
                is AppResult.Error -> {
                    // Handle error
                }
            }
        }
    }
}
