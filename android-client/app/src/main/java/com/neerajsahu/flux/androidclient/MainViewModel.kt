package com.neerajsahu.flux.androidclient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.datastore.TokenManager
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.relationship.data.remote.dto.ProfileStatsResponse
import com.neerajsahu.flux.androidclient.feature.relationship.domain.repository.RelationshipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val relationshipRepository: RelationshipRepository
) : ViewModel() {

    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<ProfileStatsResponse?>(null)
    val currentUserProfile = _currentUserProfile.asStateFlow()

    init {
        observeLoginStatus()
    }

    private fun observeLoginStatus() {
        viewModelScope.launch {
            tokenManager.getToken().collectLatest { token ->
                val loggedIn = !token.isNullOrEmpty()
                _isUserLoggedIn.value = loggedIn
                if (loggedIn) {
                    fetchCurrentUserProfile()
                } else {
                    _currentUserProfile.value = null
                }
            }
        }
    }

    private fun fetchCurrentUserProfile() {
        viewModelScope.launch {
            when (val result = relationshipRepository.getCurrentUserProfileStats()) {
                is AppResult.Success -> {
                    _currentUserProfile.value = result.data
                }
                is AppResult.Error -> {
                    // Handle error if needed
                }
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            tokenManager.deleteToken()
        }
    }
}
