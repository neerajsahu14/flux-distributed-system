package com.neerajsahu.flux.androidclient.feature.relationship.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun ProfileScreen(
    userId: Long,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToConnections: (Long, Int) -> Unit
) {
    val state = viewModel.state.value

    LaunchedEffect(userId) {
        viewModel.getProfile(userId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (state.isLoading && state.profile == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF00E5FF)
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
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FluxHeader(username = profile.username, onBackClick = onBackClick)

                Spacer(modifier = Modifier.height(24.dp))

                GlowingAvatar(profilePicUrl = profile.profilePicUrl)

                Spacer(modifier = Modifier.height(24.dp))

                ProfileActions(
                    isFollowing = profile.isFollowing,
                    onFollowClick = { viewModel.toggleFollow(userId) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                profile.bio?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
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

                PostsSection()
            }
        }
    }
}

@Composable
fun FluxHeader(username: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .size(44.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock), // Replace with back icon if available
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "@$username",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
        
        IconButton(
            onClick = { },
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .size(44.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_person),
                contentDescription = "Settings",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ProfileActions(isFollowing: Boolean, onFollowClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onFollowClick,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (isFollowing) {
                            Brush.horizontalGradient(colors = listOf(Color(0xFF1E293B), Color(0xFF334155)))
                        } else {
                            Brush.horizontalGradient(colors = listOf(Color(0xFF00E5FF), Color(0xFF32F0FF)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFollowing) "Following" else "Follow",
                    color = if (isFollowing) Color.White else Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatColumn(label = "Posts", value = posts, color = Color(0xFF00E5FF))
            Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.1f)))
            StatColumn(
                label = "Followers", 
                value = followers, 
                color = Color(0xFFE040FB),
                modifier = Modifier.clickable { onFollowersClick() }
            )
            Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.1f)))
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
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
fun GlowingAvatar(profilePicUrl: String?) {
    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .border(
                    width = 3.dp,
                    brush = Brush.sweepGradient(
                        listOf(Color(0xFF00E5FF), Color(0xFFE040FB), Color(0xFF00E5FF))
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C2128)),
            contentAlignment = Alignment.Center
        ) {
            if (profilePicUrl.isNullOrEmpty()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.fillMaxSize(0.6f)
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
                                modifier = Modifier.size(32.dp),
                                color = Color(0xFF00E5FF),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_person),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.fillMaxSize(0.6f)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun PostsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "POSTS",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color(0xFF161B22), RoundedCornerShape(16.dp)))
    }
}
