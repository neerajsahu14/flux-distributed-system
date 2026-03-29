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

    private val _loginEmail = mutableStateOf("")
    val loginEmail: State<String> = _loginEmail

    private val _loginPassword = mutableStateOf("")
    val loginPassword: State<String> = _loginPassword

    private val _signupEmail = mutableStateOf("")
    val signupEmail: State<String> = _signupEmail

    private val _signupPassword = mutableStateOf("")
    val signupPassword: State<String> = _signupPassword

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _fullName = mutableStateOf("")
    val fullName: State<String> = _fullName

    private val _bio = mutableStateOf("")
    val bio: State<String> = _bio

    private val _loginState = mutableStateOf(AuthState())
    val loginState: State<AuthState> = _loginState

    private val _signupState = mutableStateOf(AuthState())
    val signupState: State<AuthState> = _signupState

    fun onLoginEmailChange(newValue: String) {
        _loginEmail.value = newValue
    }

    fun onLoginPasswordChange(newValue: String) {
        _loginPassword.value = newValue
    }

    fun onSignupEmailChange(newValue: String) {
        _signupEmail.value = newValue
    }

    fun onSignupPasswordChange(newValue: String) {
        _signupPassword.value = newValue
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
            _loginState.value = AuthState(isLoading = true)
            val result = authRepository.login(_loginEmail.value, _loginPassword.value)
            when (result) {
                is AppResult.Success -> {
                    _loginState.value = AuthState(isSuccess = true)
                }
                is AppResult.Error -> {
                    _loginState.value = AuthState(error = result.message)
                }
            }
        }
    }

    fun signup() {
        if (_signupPassword.value != _confirmPassword.value) {
            _signupState.value = AuthState(error = "Passwords do not match")
            return
        }
        if (_signupPassword.value.length < 6) {
            _signupState.value = AuthState(error = "Password must be at least 6 characters")
            return
        }
        
        viewModelScope.launch {
            _signupState.value = AuthState(isLoading = true)
            
            val bioVal = if (_bio.value == "") null else _bio.value
            
            val request = RegisterRequestDto(
                username = _username.value,
                    fullName = _fullName.value,
                email = _signupEmail.value,
                password = _signupPassword.value,
                bio = bioVal,
                profilePicUrl = null
            )
            val result = authRepository.signup(request)
            when (result) {
                is AppResult.Success -> {
                    _signupState.value = AuthState(isSuccess = true)
                }
                is AppResult.Error -> {
                    _signupState.value = AuthState(error = result.message)
                }
            }
        }
    }
}
