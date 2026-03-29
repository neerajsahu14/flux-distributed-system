package com.neerajsahu.flux.androidclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.decode.SvgDecoder
import com.neerajsahu.flux.androidclient.core.navigation.Route
import com.neerajsahu.flux.androidclient.core.ui.theme.*
import com.neerajsahu.flux.androidclient.feature.auth.presentation.LoginScreen
import com.neerajsahu.flux.androidclient.feature.auth.presentation.SignUpScreen
import com.neerajsahu.flux.androidclient.feature.feed.presentation.CreatePostScreen
import com.neerajsahu.flux.androidclient.feature.feed.presentation.FeedScreen
import com.neerajsahu.flux.androidclient.feature.feed.presentation.PostDetailScreen
import com.neerajsahu.flux.androidclient.feature.relationship.presentation.ConnectionScreen
import com.neerajsahu.flux.androidclient.feature.relationship.presentation.EditProfileScreen
import com.neerajsahu.flux.androidclient.feature.relationship.presentation.ExploreScreen
import com.neerajsahu.flux.androidclient.feature.relationship.presentation.ProfileScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()

        setContent {
            CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                AndroidClientTheme {
                    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
                    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
                    val currentUserId = currentUserProfile?.userId

                    if (isUserLoggedIn == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = FluxCyan)
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
                                                        // If we're at the root of CreatePost tab, go to Feed
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FluxBottomDock(
    activeRoute: Route,
    onNavigate: (Route) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(72.dp)
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(36.dp), spotColor = FluxCyan.copy(alpha = 0.5f))
            .background(
                Brush.verticalGradient(
                    colors = listOf(FluxGlassWhite, FluxGlassWhite.copy(alpha = 0.05f))
                ),
                shape = RoundedCornerShape(36.dp)
            )
            .border(1.dp, FluxGlassBorder, RoundedCornerShape(36.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockIcon(
                icon = Icons.Default.Home,
                isSelected = activeRoute is Route.NewsFeed,
                onClick = { onNavigate(Route.NewsFeed) }
            )
            DockIcon(
                icon = Icons.Default.Search,
                isSelected = activeRoute is Route.Explore,
                onClick = { onNavigate(Route.Explore) }
            )
            DockIcon(
                icon = Icons.Default.Add,
                isSelected = activeRoute is Route.CreatePost,
                isCenter = true,
                onClick = { onNavigate(Route.CreatePost) }
            )
            DockIcon(
                icon = Icons.Default.Notifications,
                isSelected = activeRoute is Route.Notifications,
                onClick = { onNavigate(Route.Notifications) }
            )
            DockIcon(
                icon = Icons.Default.Person,
                isSelected = activeRoute is Route.Profile,
                onClick = { onNavigate(Route.Profile) }
            )
        }
    }
}

@Composable
fun DockIcon(
    icon: ImageVector,
    isSelected: Boolean,
    isCenter: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(if (isCenter) 56.dp else 48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.background(
                        Brush.radialGradient(
                            colors = listOf(FluxCyan.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Glowing halo
            Box(
                modifier = Modifier
                    .size(if (isCenter) 40.dp else 32.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        clip = false,
                        spotColor = FluxCyan,
                        ambientColor = FluxCyan
                    )
            )
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) FluxCyan else Color.Gray,
            modifier = Modifier.size(if (isCenter) 32.dp else 24.dp)
        )
    }
}
