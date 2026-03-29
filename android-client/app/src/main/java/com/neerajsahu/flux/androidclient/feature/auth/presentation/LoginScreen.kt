package com.neerajsahu.flux.androidclient.feature.auth.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import com.neerajsahu.flux.androidclient.R

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val state = viewModel.state.value
    if (state.isSuccess) {
        onLoginSuccess()
    }

    val scrollState = rememberScrollState()

    AuthScreenContainer(scrollState = scrollState) {
        Spacer(modifier = Modifier.height(60.dp))

        FluxLogo(iconSize = 120.dp)

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Welcome Back",
            style = TextStyle(
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = "Log in to continue",
            style = TextStyle(
                color = Color.Gray,
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        FluxInputField(
            label = "Email",
            value = viewModel.email.value,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Enter your email",
            iconResId = R.drawable.ic_person,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        FluxInputField(
            label = "Password",
            value = viewModel.password.value,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "••••••••",
            iconResId = R.drawable.ic_lock,
            isPassword = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { viewModel.login() }
            ),
            trailingContent = {
                Text(
                    text = "Forgot Password?",
                    style = TextStyle(
                        color = Color(0xFF38BDF8),
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.clickable { /* Handle Forgot Password */ }
                )
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        FluxButton(
            text = "Access Your Feed",
            onClick = viewModel::login,
            isLoading = state.isLoading
        )

        val error = state.error
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.weight(1f))

        val signUpText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Gray)) {
                append("Don't have an account? ")
            }
            withStyle(style = SpanStyle(color = Color(0xFF38BDF8))) {
                append("Sign Up")
            }
        }

        Text(
            text = signUpText,
            modifier = Modifier
                .padding(bottom = 48.dp)
                .clickable { onNavigateToSignUp() },
            textAlign = TextAlign.Center
        )
    }
}
