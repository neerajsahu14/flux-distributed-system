package com.neerajsahu.flux.androidclient.feature.auth.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import com.neerajsahu.flux.androidclient.R
import com.neerajsahu.flux.androidclient.core.ui.theme.AndroidClientTheme

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state = viewModel.state.value
    if (state.isSuccess) {
        onSignUpSuccess()
    }

    SignUpScreenContent(
        state = state,
        username = viewModel.username.value,
        fullName = viewModel.fullName.value,
        email = viewModel.email.value,
        password = viewModel.password.value,
        confirmPassword = viewModel.confirmPassword.value,
        bio = viewModel.bio.value,
        onUsernameChange = viewModel::onUsernameChange,
        onFullNameChange = viewModel::onFullNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onBioChange = viewModel::onBioChange,
        onSignUpClick = viewModel::signup,
        onNavigateToLogin = onNavigateToLogin
    )
}

@Composable
fun SignUpScreenContent(
    state: AuthState,
    username: String,
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    bio: String,
    onUsernameChange: (String) -> Unit,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val scrollState = rememberScrollState()

    AuthScreenContainer(scrollState = scrollState) {
        Spacer(modifier = Modifier.height(60.dp))

        FluxLogo(iconSize = 100.dp)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Create Account",
            style = TextStyle(
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = "Join the Flux network",
            style = TextStyle(
                color = Color.Gray,
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        FluxInputField(
            label = "Username",
            value = username,
            onValueChange = onUsernameChange,
            placeholder = "johndoe",
            iconResId = R.drawable.ic_person,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        FluxInputField(
            label = "Full Name",
            value = fullName,
            onValueChange = onFullNameChange,
            placeholder = "John Doe",
            iconResId = R.drawable.ic_person,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        FluxInputField(
            label = "Email",
            value = email,
            onValueChange = onEmailChange,
            placeholder = "john@example.com",
            iconResId = R.drawable.ic_person,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        FluxInputField(
            label = "Password",
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "••••••••",
            iconResId = R.drawable.ic_lock,
            isPassword = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        FluxInputField(
            label = "Confirm Password",
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = "••••••••",
            iconResId = R.drawable.ic_lock,
            isPassword = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        FluxInputField(
            label = "Bio (Optional)",
            value = bio,
            onValueChange = onBioChange,
            placeholder = "Tell us about yourself",
            iconResId = R.drawable.ic_person,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSignUpClick() }
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        FluxButton(
            text = "Create Account",
            onClick = onSignUpClick,
            isLoading = state.isLoading
        )

        val error = state.error
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(32.dp))

        val loginText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Gray)) {
                append("Already have an account? ")
            }
            withStyle(style = SpanStyle(color = Color(0xFF38BDF8))) {
                append("Login")
            }
        }

        Text(
            text = loginText,
            modifier = Modifier
                .padding(bottom = 48.dp)
                .clickable { onNavigateToLogin() },
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    AndroidClientTheme {
        SignUpScreenContent(
            state = AuthState(),
            username = "johndoe",
            fullName = "John Doe",
            email = "john@example.com",
            password = "password123",
            confirmPassword = "password123",
            bio = "I am a developer",
            onUsernameChange = {},
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onBioChange = {},
            onSignUpClick = {},
            onNavigateToLogin = {}
        )
    }
}
