package com.neerajsahu.flux.androidclient.feature.relationship.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neerajsahu.flux.androidclient.R
import com.neerajsahu.flux.androidclient.core.ui.theme.AndroidClientTheme
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.ProfileStats

@Composable
fun ProfileScreen(
    userId: Long,
    viewModel: ProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val state = viewModel.state.value

    LaunchedEffect(userId) {
        viewModel.getProfile(userId)
    }

    ProfileScreenContent(
        state = state,
        onBackClick = onBackClick,
        onFollowClick = { viewModel.toggleFollow(userId) }
    )
}

@Composable
fun ProfileScreenContent(
    state: ProfileState,
    onBackClick: () -> Unit,
    onFollowClick: () -> Unit
) {
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
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FluxHeader(username = profile.username)

                Spacer(modifier = Modifier.height(24.dp))

                GlowingAvatar()

                Spacer(modifier = Modifier.height(24.dp))

                ProfileActions()

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
                    followers = if (profile.followersCount >= 1000) "1.2K" else profile.followersCount.toString(),
                    following = profile.followingCount.toString()
                )

                Spacer(modifier = Modifier.height(32.dp))

                PostsSection()

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FluxHeader(username: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "@$username",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        
        IconButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .size(44.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_person),
                contentDescription = "Profile",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun GlowingAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val color = Color(0xFF00E5FF).copy(alpha = 0.15f)
            
            val path1 = Path().apply {
                moveTo(0f, height * 0.4f)
                quadraticTo(width * 0.25f, height * 0.35f, width * 0.5f, height * 0.45f)
                quadraticTo(width * 0.75f, height * 0.55f, width, height * 0.5f)
            }
            val path2 = Path().apply {
                moveTo(0f, height * 0.6f)
                quadraticTo(width * 0.25f, height * 0.65f, width * 0.5f, height * 0.55f)
                quadraticTo(width * 0.75f, height * 0.45f, width, height * 0.5f)
            }
            
            drawPath(path1, color, style = Stroke(width = 1.dp.toPx()))
            drawPath(path2, color, style = Stroke(width = 1.dp.toPx()))
        }

        Box(
            modifier = Modifier
                .size(160.dp)
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
                .size(144.dp)
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), CircleShape)
        )
        
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C2128)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_person),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.fillMaxSize(0.6f)
            )
        }
    }
}

@Composable
fun ProfileActions(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { },
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
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF00E5FF), Color(0xFF32F0FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.edit_profile),
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Box(
            modifier = Modifier
                .size(52.dp)
                .border(1.dp, Color(0xFF00E5FF), CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFF00E5FF).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(12.dp).background(Color(0xFF00E5FF), CircleShape))
            }
        }
    }
}

@Composable
fun StatsCard(posts: String, followers: String, following: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            StatColumn(label = stringResource(R.string.posts), value = posts, color = Color(0xFF00E5FF))
            Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.1f)))
            StatColumn(label = stringResource(R.string.followers), value = followers, color = Color(0xFFE040FB))
            Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.1f)))
            StatColumn(label = stringResource(R.string.following), value = following, color = Color(0xFF38BDF8))
        }
    }
}

@Composable
fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
fun PostsSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.posts).uppercase(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
             Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                 PostItem(
                     isLeft = true,
                     modifier = Modifier.weight(1f)
                 )
                 PostItem(
                     isLeft = false,
                     modifier = Modifier.weight(1f)
                 )
             }
             Box(
                 modifier = Modifier
                    .size(10.dp)
                    .background(Color(0xFF00E5FF), CircleShape)
                    .border(2.dp, Color(0xFF060D15), CircleShape)
             )
        }
    }
}

@Composable
fun PostItem(isLeft: Boolean, modifier: Modifier = Modifier) {
    val shape = if (isLeft) {
        RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp, topEnd = 12.dp, bottomEnd = 12.dp)
    } else {
        RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp, topStart = 12.dp, bottomStart = 12.dp)
    }
    
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(shape)
            .background(Color(0xFF1C2128))
            .border(1.dp, Color.White.copy(alpha = 0.1f), shape)
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    AndroidClientTheme {
        ProfileScreenContent(
            state = ProfileState(
                profile = ProfileStats(
                    userId = 1L,
                    username = "neerajsahu",
                    fullName = "Neeraj Sahu",
                    bio = "Bhai. full-stack engineering case study. Kotlin, Spring Boot, DDD, Cloudinary, Idempotency and Fluxing.",
                    postCount = 128,
                    followersCount = 1200,
                    followingCount = 98,
                    isFollowing = false
                )
            ),
            onBackClick = {},
            onFollowClick = {}
        )
    }
}
