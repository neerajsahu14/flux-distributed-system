package com.neerajsahu.flux.androidclient.feature.relationship.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.neerajsahu.flux.androidclient.R
import com.neerajsahu.flux.androidclient.core.ui.components.FluxLineBackground
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxBackgroundDark
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxCyan
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.Post

@Composable
fun ProfileScreen(
    userId: Long,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onPostClick: (Long) -> Unit = {},
    onNavigateToConnections: (Long, Int) -> Unit,
    onEditProfileClick: () -> Unit = {}
) {
    val state = viewModel.state.value

    LaunchedEffect(userId) {
        viewModel.getProfile(userId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FluxBackgroundDark)
    ) {
        // Flux Line Background for consistency
        FluxLineBackground(modifier = Modifier.fillMaxSize())

        if (state.isLoading && state.profile == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = FluxCyan
            )
        } else if (state.error != null && state.profile == null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (state.profile != null) {
            val profile = state.profile
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FluxHeader(username = profile.username, onBackClick = onBackClick)

                Spacer(modifier = Modifier.height(24.dp))

                GlowingAvatar(profilePicUrl = profile.profilePicUrl)

                Spacer(modifier = Modifier.height(24.dp))

                ProfileActions(
                    isCurrentUser = state.isCurrentUser,
                    isFollowing = profile.isFollowing,
                    onFollowClick = { viewModel.toggleFollow(userId) },
                    onEditClick = onEditProfileClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                profile.bio?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                StatsCard(
                    posts = profile.postCount.toString(),
                    followers = if (profile.followersCount >= 1000) "${(profile.followersCount / 1000.0)}K" else profile.followersCount.toString(),
                    following = profile.followingCount.toString(),
                    onFollowersClick = { onNavigateToConnections(userId, 0) },
                    onFollowingClick = { onNavigateToConnections(userId, 1) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                PostsSection(
                    posts = state.posts,
                    isLoading = state.isPostsLoading,
                    onPostClick = onPostClick
                )
            }
        }
    }
}

@Composable
fun FluxHeader(username: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = "@$username",
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(
            onClick = { },
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ProfileActions(
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = if (isCurrentUser) onEditClick else onFollowClick,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (isCurrentUser) {
                            Brush.horizontalGradient(colors = listOf(Color(0xFF475569), Color(0xFF64748B)))
                        } else if (isFollowing) {
                            Brush.horizontalGradient(colors = listOf(Color(0xFF1E293B), Color(0xFF334155)))
                        } else {
                            Brush.horizontalGradient(colors = listOf(FluxCyan, Color(0xFF32F0FF)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCurrentUser) "Edit Profile" else if (isFollowing) "Following" else "Follow",
                    color = if (isCurrentUser || isFollowing) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    posts: String, 
    followers: String, 
    following: String, 
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatColumn(label = "Posts", value = posts, color = FluxCyan)
            Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.1f)))
            StatColumn(
                label = "Followers", 
                value = followers, 
                color = Color(0xFFE040FB),
                modifier = Modifier.clickable { onFollowersClick() }
            )
            Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.1f)))
            StatColumn(
                label = "Following", 
                value = following, 
                color = Color(0xFF38BDF8),
                modifier = Modifier.clickable { onFollowingClick() }
            )
        }
    }
}

@Composable
fun StatColumn(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
fun GlowingAvatar(profilePicUrl: String?) {
    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow/border
        Box(
            modifier = Modifier
                .size(136.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(FluxCyan, Color(0xFFE040FB), FluxCyan)
                    ),
                    shape = CircleShape
                )
        )
        // Avatar container
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C2128)),
            contentAlignment = Alignment.Center
        ) {
            if (profilePicUrl.isNullOrEmpty()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.fillMaxSize(0.5f)
                )
            } else {
                SubcomposeAsyncImage(
                    model = profilePicUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = FluxCyan,
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_person),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.fillMaxSize(0.5f)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun PostsSection(
    posts: List<Post>,
    isLoading: Boolean,
    onPostClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "POSTS",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading && posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FluxCyan)
            }
        } else if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No posts yet", color = Color.Gray)
            }
        } else {
            // Displaying posts in a grid.
            val chunkedPosts = posts.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chunkedPosts.forEach { rowPosts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPosts.forEach { post ->
                            PostGridItem(
                                post = post,
                                modifier = Modifier.weight(1f),
                                onClick = { onPostClick(post.id) }
                            )
                        }
                        // Fill empty spots in the last row if needed
                        repeat(3 - rowPosts.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostGridItem(post: Post, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
    ) {
        SubcomposeAsyncImage(
            model = post.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = FluxCyan,
                        strokeWidth = 1.dp
                    )
                }
            }
        )
    }
}
