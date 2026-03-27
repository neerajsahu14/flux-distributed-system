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
import com.neerajsahu.flux.androidclient.feature.feed.presentation.FeedScreen
import com.neerajsahu.flux.androidclient.feature.relationship.presentation.ConnectionScreen
import com.neerajsahu.flux.androidclient.feature.relationship.presentation.ProfileScreen
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
                val currentUserProfile by viewModel.currentUserProfile.collectAsState()
                val currentUserId = currentUserProfile?.profile?.id ?: 0L
                
                if (isUserLoggedIn == null) {
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
                    val showBottomBar = currentRoute != null && (
                        currentRoute is Route.NewsFeed || 
                        currentRoute is Route.Profile || 
                        currentRoute is Route.UserProfile || 
                        currentRoute is Route.Connections
                    )

                    // Determine which tab is "active" based on the first element in the stack (NewsFeed or Profile)
                    val activeRoot = backStack.firstOrNull { it is Route.NewsFeed || it is Route.Profile } ?: Route.NewsFeed

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (showBottomBar && currentRoute != null) {
                                BottomNavigationBar(
                                    activeRoot = activeRoot,
                                    onNavigate = { route ->
                                        if (activeRoot != route) {
                                            backStack.clear()
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
                                        FeedScreen(
                                            onProfileClick = { clickedUserId ->
                                                if (clickedUserId == currentUserId) {
                                                    backStack.clear()
                                                    backStack.add(Route.Profile)
                                                } else {
                                                    backStack.add(Route.UserProfile(clickedUserId))
                                                }
                                            }
                                        )
                                    }
                                    Route.Profile -> NavEntry(Route.Profile) {
                                        ProfileScreen(
                                            userId = currentUserId,
                                            onBackClick = {
                                                if (backStack.size > 1) {
                                                    backStack.removeAt(backStack.size - 1)
                                                }
                                            },
                                            onNavigateToConnections = { userId, initialTab ->
                                                backStack.add(Route.Connections(userId, initialTab))
                                            }
                                        )
                                    }
                                    is Route.UserProfile -> NavEntry(route) {
                                        ProfileScreen(
                                            userId = route.userId,
                                            onBackClick = {
                                                if (backStack.size > 1) {
                                                    backStack.removeAt(backStack.size - 1)
                                                }
                                            },
                                            onNavigateToConnections = { userId, initialTab ->
                                                backStack.add(Route.Connections(userId, initialTab))
                                            }
                                        )
                                    }
                                    is Route.Connections -> NavEntry(route) {
                                        ConnectionScreen(
                                            userId = route.userId,
                                            initialTab = route.initialTab,
                                            onBackClick = {
                                                if (backStack.size > 1) {
                                                    backStack.removeAt(backStack.size - 1)
                                                }
                                            },
                                            onProfileClick = { clickedUserId: Long ->
                                                if (clickedUserId == currentUserId) {
                                                    backStack.clear()
                                                    backStack.add(Route.Profile)
                                                } else {
                                                    backStack.add(Route.UserProfile(clickedUserId))
                                                }
                                            }
                                        )
                                    }
                                    Route.Main -> NavEntry(Route.Main) {
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
    activeRoot: Route,
    onNavigate: (Route) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = activeRoot is Route.NewsFeed,
            onClick = { onNavigate(Route.NewsFeed) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = activeRoot is Route.Profile,
            onClick = { onNavigate(Route.Profile) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
