package com.neerajsahu.flux.androidclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.neerajsahu.flux.androidclient.core.navigation.Route
import com.neerajsahu.flux.androidclient.core.ui.theme.AndroidClientTheme
import com.neerajsahu.flux.androidclient.feature.auth.presentation.LoginScreen
import com.neerajsahu.flux.androidclient.feature.auth.presentation.SignUpScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidClientTheme {
                val backStack = remember { mutableStateListOf<Route>(Route.Login) }
                
                BackHandler(enabled = backStack.size > 1) {
                    backStack.removeAt(backStack.size - 1)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavDisplay(
                        backStack = backStack,
                        modifier = Modifier.padding(innerPadding),
                        onBack = { 
                            if (backStack.size > 1) {
                                backStack.removeAt(backStack.size - 1)
                            }
                        },
                        entryProvider = { route ->
                            when (route) {
                                Route.Login -> NavEntry(Route.Login) {
                                    LoginScreen(
                                        onLoginSuccess = {
                                            backStack.clear()
                                            backStack.add(Route.Home)
                                        },
                                        onNavigateToSignUp = {
                                            backStack.add(Route.SignUp)
                                        }
                                    )
                                }
                                Route.SignUp -> NavEntry(Route.SignUp) {
                                    SignUpScreen(
                                        onSignUpSuccess = {
                                            backStack.clear()
                                            backStack.add(Route.Home)
                                        },
                                        onNavigateToLogin = {
                                            backStack.removeAt(backStack.size - 1)
                                        }
                                    )
                                }
                                Route.Home -> NavEntry(Route.Home) {
                                    Text("Home Screen (Work in Progress)")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
