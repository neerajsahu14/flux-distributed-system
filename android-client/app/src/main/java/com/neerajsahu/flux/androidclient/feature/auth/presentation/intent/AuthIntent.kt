package com.neerajsahu.flux.androidclient.feature.auth.presentation.intent

sealed class AuthIntent {
    data class LoginEmailChanged(val value: String) : AuthIntent()
    data class LoginPasswordChanged(val value: String) : AuthIntent()

    data class SignupEmailChanged(val value: String) : AuthIntent()
    data class SignupPasswordChanged(val value: String) : AuthIntent()
    data class ConfirmPasswordChanged(val value: String) : AuthIntent()
    data class UsernameChanged(val value: String) : AuthIntent()
    data class FullNameChanged(val value: String) : AuthIntent()
    data class BioChanged(val value: String) : AuthIntent()

    object Login : AuthIntent()
    object Signup : AuthIntent()
}

