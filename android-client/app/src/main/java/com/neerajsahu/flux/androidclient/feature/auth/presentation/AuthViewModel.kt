package com.neerajsahu.flux.androidclient.feature.auth.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neerajsahu.flux.androidclient.core.utils.AppResult
import com.neerajsahu.flux.androidclient.feature.auth.data.remote.dto.RegisterRequestDto
import com.neerajsahu.flux.androidclient.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _fullName = mutableStateOf("")
    val fullName: State<String> = _fullName

    private val _bio = mutableStateOf("")
    val bio: State<String> = _bio

    private val _state = mutableStateOf(AuthState())
    val state: State<AuthState> = _state

    fun onEmailChange(newValue: String) {
        _email.value = newValue
    }

    fun onPasswordChange(newValue: String) {
        _password.value = newValue
    }

    fun onConfirmPasswordChange(newValue: String) {
        _confirmPassword.value = newValue
    }

    fun onUsernameChange(newValue: String) {
        _username.value = newValue
    }

    fun onFullNameChange(newValue: String) {
        _fullName.value = newValue
    }

    fun onBioChange(newValue: String) {
        _bio.value = newValue
    }

    fun login() {
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            val result = authRepository.login(_email.value, _password.value)
            when (result) {
                is AppResult.Success -> {
                    _state.value = AuthState(isSuccess = true)
                }
                is AppResult.Error -> {
                    _state.value = AuthState(error = result.message)
                }
            }
        }
    }

    fun signup() {
        if (_password.value != _confirmPassword.value) {
            _state.value = AuthState(error = "Passwords do not match")
            return
        }
        if (_password.value.length < 6) {
            _state.value = AuthState(error = "Password must be at least 6 characters")
            return
        }
        
        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            
            val bioVal = if (_bio.value == "") null else _bio.value
            
            val request = RegisterRequestDto(
                username = _username.value,
                    fullName = _fullName.value,
                email = _email.value,
                password = _password.value,
                bio = bioVal,
                profilePicUrl = null
            )
            val result = authRepository.signup(request)
            when (result) {
                is AppResult.Success -> {
                    _state.value = AuthState(isSuccess = true)
                }
                is AppResult.Error -> {
                    _state.value = AuthState(error = result.message)
                }
            }
        }
    }
}
