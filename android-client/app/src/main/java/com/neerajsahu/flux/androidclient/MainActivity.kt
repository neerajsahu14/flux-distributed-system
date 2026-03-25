package com.neerajsahu.flux.androidclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidClientTheme {
                val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
                
                if (isUserLoggedIn == null) {
                    // Show a splash or loading screen while checking auth status
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val backStack = remember { 
                        mutableStateListOf<Route>(if (isUserLoggedIn == true) Route.NewsFeed else Route.Login) 
                    }
                    
                    BackHandler(enabled = backStack.size > 1) {
                        backStack.removeAt(backStack.size - 1)
                    }

                    val currentRoute = backStack.lastOrNull()
                    val showBottomBar = currentRoute != null && (currentRoute is Route.Main || currentRoute is Route.NewsFeed || currentRoute is Route.Profile)

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (showBottomBar && currentRoute != null) {
                                BottomNavigationBar(
                                    currentRoute = currentRoute,
                                    onNavigate = { route ->
                                        if (backStack.isNotEmpty() && backStack.last() != route) {
                                            backStack.removeAt(backStack.size - 1)
                                            backStack.add(route)
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
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
                                                backStack.add(Route.NewsFeed)
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
                                                backStack.add(Route.NewsFeed)
                                            },
                                            onNavigateToLogin = {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        )
                                    }
                                    Route.NewsFeed -> NavEntry(Route.NewsFeed) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            Text("News Feed Screen")
                                        }
                                    }
                                    Route.Profile -> NavEntry(Route.Profile) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            Text("Profile Screen")
                                        }
                                    }
                                    Route.Main -> NavEntry(Route.Main) {
                                        // Handle Main by ensuring we have a default tab
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            Text("Main")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: Route,
    onNavigate: (Route) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute is Route.NewsFeed,
            onClick = { onNavigate(Route.NewsFeed) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentRoute is Route.Profile,
            onClick = { onNavigate(Route.Profile) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
