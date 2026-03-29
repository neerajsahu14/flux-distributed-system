package com.neerajsahu.flux.androidclient.feature.relationship.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.auth.domain.model.User
import com.neerajsahu.flux.androidclient.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class EditProfileState(
    val user: User? = null,
    val bio: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()

    init {
        repository.getProfile().onEach { user ->
            _state.update { it.copy(user = user, bio = user?.bio ?: "") }
        }.launchIn(viewModelScope)
    }

    fun onBioChanged(newBio: String) {
        _state.update { it.copy(bio = newBio) }
    }

    fun updateBio() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.updateBio(_state.value.bio)) {
                is AppResult.Success<User> -> {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun updateProfileImage(imageFile: File) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.updateProfileImage(imageFile)) {
                is AppResult.Success<User> -> {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun consumeSuccess() {
        _state.update { it.copy(isSuccess = false) }
    }
}
