package com.neerajsahu.flux.androidclient.core.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.neerajsahu.flux.androidclient.R
import com.neerajsahu.flux.androidclient.core.navigation.Route
import com.neerajsahu.flux.androidclient.core.ui.components.FluxBottomDock
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxBackgroundDark
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxCyan
import com.neerajsahu.flux.androidclient.feature.auth.presentation.LoginScreen
import com.neerajsahu.flux.androidclient.feature.auth.presentation.SignUpScreen
import com.neerajsahu.flux.androidclient.feature.feed.presentation.CreatePostScreen
import com.neerajsahu.flux.androidclient.feature.feed.presentation.FeedScreen
import com.neerajsahu.flux.androidclient.feature.feed.presentation.PostDetailScreen
import com.neerajsahu.flux.androidclient.feature.relationship.presentation.ConnectionScreen
import com.neerajsahu.flux.androidclient.feature.relationship.presentation.EditProfileScreen
import com.neerajsahu.flux.androidclient.feature.relationship.presentation.ExploreScreen
import com.neerajsahu.flux.androidclient.feature.relationship.presentation.ProfileScreen
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun FluxApp(
    isUserLoggedIn: Boolean?,
    currentUserId: Long?,
    isConnected: Boolean
) {
    var wasDisconnected by remember { mutableStateOf(false) }

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            wasDisconnected = true
        }
    }

    if (isUserLoggedIn != null) {
        val routeSaver = listSaver<androidx.compose.runtime.snapshots.SnapshotStateList<Route>, String>(
            save = { it.map { route -> Json.encodeToString(route) } },
            restore = { it.map { json -> Json.decodeFromString<Route>(json) }.toMutableStateList() as androidx.compose.runtime.snapshots.SnapshotStateList<Route> }
        )

        val backStack = rememberSaveable(saver = routeSaver) {
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
                currentRoute is Route.Connections ||
                currentRoute is Route.Explore ||
                currentRoute is Route.Notifications ||
                currentRoute is Route.CreatePost
            )

        val activeRoot = backStack.firstOrNull { 
            it is Route.NewsFeed || it is Route.Profile || it is Route.Explore || it is Route.Notifications || it is Route.CreatePost
        } ?: Route.NewsFeed

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    FluxBottomDock(
                        activeRoute = activeRoot,
                        onNavigate = { route ->
                            if (activeRoot != route) {
                                backStack.clear()
                                backStack.add(route)
                            }
                        }
                    )
                }
            },
            containerColor = FluxBackgroundDark
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
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
                                        if (currentUserId != null && clickedUserId == currentUserId) {
                                            backStack.clear()
                                            backStack.add(Route.Profile)
                                        } else {
                                            backStack.add(Route.UserProfile(clickedUserId))
                                        }
                                    },
                                    onPostClick = { postId ->
                                        backStack.add(Route.PostDetail(postId))
                                    }
                                )
                            }
                            Route.Profile -> NavEntry(Route.Profile) {
                                if (currentUserId != null && currentUserId > 0L) {
                                    ProfileScreen(
                                        userId = currentUserId,
                                        onBackClick = {
                                            if (backStack.size > 1) {
                                                backStack.removeAt(backStack.size - 1)
                                            }
                                        },
                                        onPostClick = { postId ->
                                            backStack.add(Route.PostDetail(postId))
                                        },
                                        onNavigateToConnections = { userId, initialTab ->
                                            backStack.add(Route.Connections(userId, initialTab))
                                        },
                                        onEditProfileClick = {
                                            backStack.add(Route.EditProfile)
                                        }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = FluxCyan)
                                    }
                                }
                            }
                            is Route.UserProfile -> NavEntry(route) {
                                ProfileScreen(
                                    userId = route.userId,
                                    onBackClick = {
                                        if (backStack.size > 1) {
                                            backStack.removeAt(backStack.size - 1)
                                        }
                                    },
                                    onPostClick = { postId ->
                                        backStack.add(Route.PostDetail(postId))
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
                                        if (currentUserId != null && clickedUserId == currentUserId) {
                                            backStack.clear()
                                            backStack.add(Route.Profile)
                                        } else {
                                            backStack.add(Route.UserProfile(clickedUserId))
                                        }
                                    }
                                )
                            }
                            is Route.PostDetail -> NavEntry(route) {
                                PostDetailScreen(
                                    postId = route.postId,
                                    onBackClick = {
                                        if (backStack.size > 1) {
                                            backStack.removeAt(backStack.size - 1)
                                        }
                                    },
                                    onProfileClick = { clickedUserId ->
                                        if (currentUserId != null && clickedUserId == currentUserId) {
                                            backStack.clear()
                                            backStack.add(Route.Profile)
                                        } else {
                                            backStack.add(Route.UserProfile(clickedUserId))
                                        }
                                    }
                                )
                            }
                            Route.Explore -> NavEntry(Route.Explore) {
                                ExploreScreen(
                                    onProfileClick = { clickedUserId ->
                                        if (currentUserId != null && clickedUserId == currentUserId) {
                                            backStack.clear()
                                            backStack.add(Route.Profile)
                                        } else {
                                            backStack.add(Route.UserProfile(clickedUserId))
                                        }
                                    }
                                )
                            }
                            Route.Notifications -> NavEntry(Route.Notifications) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("Notifications Screen", color = Color.White)
                                }
                            }
                            Route.CreatePost -> NavEntry(Route.CreatePost) {
                                CreatePostScreen(
                                    onBackClick = {
                                        if (backStack.size > 1) {
                                            backStack.removeAt(backStack.size - 1)
                                        } else {
                                            backStack.clear()
                                            backStack.add(Route.NewsFeed)
                                        }
                                    },
                                    onPostCreated = {
                                        backStack.clear()
                                        backStack.add(Route.NewsFeed)
                                    }
                                )
                            }
                            Route.EditProfile -> NavEntry(Route.EditProfile) {
                                EditProfileScreen(
                                    onBackClick = {
                                        if (backStack.size > 1) {
                                            backStack.removeAt(backStack.size - 1)
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

                // Network Status Bar Overlay
                AnimatedVisibility(
                    visible = !isConnected || (isConnected && wasDisconnected),
                    enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(500)),
                    exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(500)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                ) {
                    val bgColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFFE53935)
                    val text = if (isConnected) stringResource(R.string.back_online) else stringResource(R.string.no_connection)
                    
                    LaunchedEffect(isConnected) {
                        if (isConnected && wasDisconnected) {
                            delay(3000)
                            wasDisconnected = false
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = text, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
