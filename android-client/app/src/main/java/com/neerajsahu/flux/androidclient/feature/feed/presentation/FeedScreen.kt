package com.neerajsahu.flux.androidclient.feature.feed.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import com.neerajsahu.flux.androidclient.R
import com.neerajsahu.flux.androidclient.core.ui.components.FluxLineBackground
import com.neerajsahu.flux.androidclient.core.ui.components.GlassCard
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxCyan
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxRuby
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxGlassWhite
import com.neerajsahu.flux.androidclient.core.ui.components.shimmerEffect
import com.neerajsahu.flux.androidclient.feature.interaction.presentation.components.InteractionBar
import com.neerajsahu.flux.androidclient.feature.feed.data.mapper.formatFeedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onProfileClick: (Long) -> Unit,
    onPostClick: (Long) -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (state.posts.isEmpty()) {
            viewModel.loadGlobalFeed()
        }
    }

    LaunchedEffect(selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> viewModel.loadGlobalFeed()
            1 -> viewModel.loadTimelineFeed()
        }
    }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // The "Flux Line" Timeline Background
            FluxLineBackground()

            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Custom Button-style Tabs matching ConnectionScreen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(48.dp)
                        .background(Color(0xFF1E293B).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeedTabItem(
                        text = "Global",
                        isSelected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.1f)))
                    FeedTabItem(
                        text = "Timeline",
                        isSelected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.newPostsAvailable > 0) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                viewModel.applyNewPosts()
                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FluxCyan),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("${state.newPostsAvailable} New Posts Available", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                when {
                    state.isLoading && state.posts.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = FluxCyan)
                        }
                    }
                    state.error != null && state.posts.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = state.error ?: "Failed to load feed",
                                    color = FluxRuby,
                                    modifier = Modifier.padding(24.dp)
                                )
                                TextButton(onClick = viewModel::refresh) {
                                    Text("Retry", color = FluxCyan)
                                }
                            }
                        }
                    }
                    else -> {
                        PullToRefreshBox(
                            isRefreshing = state.isRefreshing,
                            onRefresh = { viewModel.refresh() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                itemsIndexed(state.posts, key = { _, post -> post.id }) { index, post ->
                                    // Pagination trigger
                                    if (index == state.posts.lastIndex && state.hasMore) {
                                        LaunchedEffect(index) {
                                            viewModel.loadMorePosts()
                                        }
                                    }
                                    
                                    val isLeftAligned = index % 2 == 0
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                        contentAlignment = if (isLeftAligned) Alignment.CenterStart else Alignment.CenterEnd
                                    ) {
                                        FluxFeedPostCard(
                                            post = post,
                                            isLeftAligned = isLeftAligned,
                                            onProfileClick = onProfileClick,
                                            onPostClick = onPostClick,
                                            isInteractionInFlight = state.interactionInFlightPostIds.contains(post.id),
                                            onLikeClick = { viewModel.onLikeClick(post.id) },
                                            onBookmarkClick = { viewModel.onBookmarkClick(post.id) },
                                            onShareClick = { viewModel.onShareClick(post.id) }
                                        )
                                    }
                                }
                                
                                if (state.isLoadingMore) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = FluxCyan, modifier = Modifier.size(32.dp))
                                        }
                                    }
                                } else if (!state.hasMore && state.posts.isNotEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No more posts at the moment",
                                                color = Color.Gray,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

}

@Composable
fun FeedTabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Cache the Paint object to prevent GC thrashing during draw phase
    val density = LocalDensity.current
    val glowPaint = remember(density) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.TRANSPARENT
            // Adjust radius (20dp -> 12dp) and add alpha (0.6f) to soften the core shadow
            setShadowLayer(
                with(density) { 12.dp.toPx() },
                0f,
                0f,
                FluxCyan.copy(alpha = 0.6f).toArgb()
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .drawBehind {
                if (isSelected) {
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(
                            0f, 0f, size.width, size.height,
                            24.dp.toPx(), 24.dp.toPx(),
                            glowPaint
                        )
                    }
                }
            }
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        // 2. Reduce surface tint opacity (0.15f -> 0.08f) for subtlety
                        colors = listOf(FluxCyan.copy(alpha = 0.08f), Color.Transparent)
                    )
                } else {
                    SolidColor(Color.Transparent) // Cleaner than an empty linear gradient
                },
                RoundedCornerShape(24.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(FluxCyan, RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

@Composable
fun FluxFeedPostCard(
    post: com.neerajsahu.flux.androidclient.feature.feed.domain.model.Post,
    isLeftAligned: Boolean,
    onProfileClick: (Long) -> Unit,
    onPostClick: (Long) -> Unit,
    isInteractionInFlight: Boolean,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit
) {
    // Glassmorphic panel with asymmetric sizing and positioning
    GlassCard(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(
                start = if (isLeftAligned) 0.dp else 16.dp,
                end = if (isLeftAligned) 16.dp else 0.dp
            )
            .clickable { onPostClick(post.id) }
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // User Avatar as a "Node" - matching ProfileScreen logic
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, FluxCyan, CircleShape)
                        .background(Color(0xFF1C2128))
                        .clickable { onProfileClick(post.author.id) },
                    contentAlignment = Alignment.Center
                ) {
                    if (post.author.profilePicUrl.isNullOrEmpty()) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_person),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.fillMaxSize(0.6f)
                        )
                    } else {
                        SubcomposeAsyncImage(
                            model = post.author.profilePicUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(modifier = Modifier.fillMaxSize().shimmerEffect())
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

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "@${post.author.username}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FluxCyan
                        )
                    )
                    Text(
                        text = formatFeedDateTime(post.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Media area: Coil with loading state or profile fallback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(0.5.dp, FluxCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .background(FluxGlassWhite),
                contentAlignment = Alignment.Center
            ) {
                if (!post.imageUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = post.imageUrl,
                        contentDescription = post.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                        },
                        error = {
                            ProfileFallbackNode(profilePicUrl = post.author.profilePicUrl)
                        }
                    )
                } else {
                    ProfileFallbackNode(profilePicUrl = post.author.profilePicUrl)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!post.caption.isNullOrBlank()) {
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            InteractionBar(
                isLiked = post.isLiked,
                isBookmarked = post.isBookmarked,
                likeCount = post.likeCount,
                shareCount = post.shareCount,
                isInteractionInFlight = isInteractionInFlight,
                onLikeClick = onLikeClick,
                onBookmarkClick = onBookmarkClick,
                onShareClick = onShareClick
            )
        }
    }
}

@Composable
private fun ProfileFallbackNode(profilePicUrl: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Large fallback avatar using same logic as ProfileScreen's GlowingAvatar but simplified
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(94.dp)
                    .border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFFE040FB), Color(0xFF00E5FF))
                        ),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(84.dp)
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
                            Box(modifier = Modifier.fillMaxSize().shimmerEffect())
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
}
