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
    val pendingImageUri: android.net.Uri? = null,
    val pendingImageFile: File? = null,
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

    fun onImageSelected(uri: android.net.Uri, file: File) {
        _state.update { it.copy(pendingImageUri = uri, pendingImageFile = file) }
    }

    fun updateProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            var hasError = false

            // Update Image if selected
            if (_state.value.pendingImageFile != null) {
                when (val result = repository.updateProfileImage(_state.value.pendingImageFile!!)) {
                    is AppResult.Error -> {
                        _state.update { it.copy(error = result.message) }
                        hasError = true
                    }
                    is AppResult.Success -> {
                        // Success handled below
                    }
                }
            }

            // Update Bio
            if (!hasError) {
                when (val result = repository.updateBio(_state.value.bio)) {
                    is AppResult.Success<User> -> {
                        _state.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                    is AppResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            } else {
                _state.update { it.copy(isLoading = false) }
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
