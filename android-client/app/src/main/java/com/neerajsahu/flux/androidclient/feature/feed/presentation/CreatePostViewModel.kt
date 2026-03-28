package com.neerajsahu.flux.androidclient.feature.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.feed.domain.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.util.UUID
import javax.inject.Inject

data class CreatePostUiState(
    val caption: String = "",
    val selectedMediaUri: String? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePostUiState())
    val state = _state.asStateFlow()

    fun onCaptionChanged(caption: String) {
        _state.update { it.copy(caption = caption, errorMessage = null) }
    }

    fun onMediaSelected(uri: String?) {
        _state.update { it.copy(selectedMediaUri = uri, errorMessage = null) }
    }

    fun clearSelectedMedia() {
        _state.update { it.copy(selectedMediaUri = null, errorMessage = null) }
    }

    fun createPost(mediaPart: MultipartBody.Part) {
        val current = _state.value
        if (current.isSubmitting) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null
                )
            }

            when (
                feedRepository.createPost(
                    image = mediaPart,
                    caption = current.caption.ifBlank { null },
                    requestId = UUID.randomUUID().toString()
                )
            ) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            isSuccess = true,
                            errorMessage = null
                        )
                    }
                }

                is AppResult.Error -> {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = "Failed to upload media. Please try again.",
                            isSuccess = false
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun setError(message: String) {
        _state.update { it.copy(errorMessage = message) }
    }

    fun consumeSuccess() {
        _state.update { current ->
            if (!current.isSuccess) current else current.copy(isSuccess = false)
        }
    }
}


