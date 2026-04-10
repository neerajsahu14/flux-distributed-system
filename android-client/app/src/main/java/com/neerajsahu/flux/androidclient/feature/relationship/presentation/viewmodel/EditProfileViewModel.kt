package com.neerajsahu.flux.androidclient.feature.relationship.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.network.AppResult
import com.neerajsahu.flux.androidclient.feature.auth.domain.model.User
import com.neerajsahu.flux.androidclient.feature.auth.domain.repository.AuthRepository
import com.neerajsahu.flux.androidclient.feature.relationship.presentation.intent.EditProfileIntent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class EditProfileState(
    val user: User? = null,
    val bio: String = "",
    val pendingImageUri: Uri? = null,
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

    fun onIntent(intent: EditProfileIntent) {
        when (intent) {
            is EditProfileIntent.BioChanged -> onBioChanged(intent.bio)
            is EditProfileIntent.ImageSelected -> onImageSelected(intent.uri, intent.file)
            EditProfileIntent.UpdateProfile -> updateProfile()
            EditProfileIntent.ClearError -> clearError()
            EditProfileIntent.ConsumeSuccess -> consumeSuccess()
        }
    }

    private fun onBioChanged(newBio: String) {
        _state.update { it.copy(bio = newBio) }
    }

    private fun onImageSelected(uri: Uri, file: File) {
        _state.update { it.copy(pendingImageUri = uri, pendingImageFile = file) }
    }

    private fun updateProfile() {
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
    
    private fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    private fun consumeSuccess() {
        _state.update { it.copy(isSuccess = false) }
    }
}
