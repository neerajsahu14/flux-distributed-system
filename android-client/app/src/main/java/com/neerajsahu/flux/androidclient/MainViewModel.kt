package com.neerajsahu.flux.androidclient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.datastore.TokenManager
import com.neerajsahu.flux.androidclient.core.network.AuthEventManager
import com.neerajsahu.flux.androidclient.core.network.ConnectivityObserver
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.ProfileStats
import com.neerajsahu.flux.androidclient.feature.relationship.domain.repository.RelationshipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val relationshipRepository: RelationshipRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<ProfileStats?>(null)
    val currentUserProfile = _currentUserProfile.asStateFlow()

    val isConnected = connectivityObserver.isConnected

    init {
        observeLoginStatus()
        observeAuthEvents()
    }

    private fun observeAuthEvents() {
        viewModelScope.launch {
            AuthEventManager.unauthorizedEvent.collectLatest {
                logout()
            }
        }
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
        relationshipRepository.getCurrentUserProfileStats().onEach { result ->
            when (result) {
                is AppResult.Success -> {
                    _currentUserProfile.value = result.data
                }
                is AppResult.Error -> {
                    // Handle error if needed
                }
            }
        }.launchIn(viewModelScope)
    }
    
    fun logout() {
        viewModelScope.launch {
            tokenManager.deleteToken()
        }
    }
}
