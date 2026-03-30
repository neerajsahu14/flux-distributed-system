package com.neerajsahu.flux.androidclient.feature.feed.presentation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.neerajsahu.flux.androidclient.core.ui.components.shimmerEffect
import coil.compose.SubcomposeAsyncImage
import com.neerajsahu.flux.androidclient.core.ui.components.FluxLineBackground
import com.neerajsahu.flux.androidclient.R
import com.neerajsahu.flux.androidclient.core.ui.components.GlassCard
import com.neerajsahu.flux.androidclient.core.ui.theme.*
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.MediaType
import com.neerajsahu.flux.androidclient.feature.feed.presentation.model.AttachmentUiState
import com.neerajsahu.flux.androidclient.feature.feed.presentation.model.PostUiState
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun PostDetailScreen(
    postId: Long,
    onBackClick: () -> Unit,
    onProfileClick: (Long) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FluxBackgroundDark)
    ) {
        // Dynamic Flux Line Background
        FluxLineBackground(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            // Centered Header (Replacing Scaffold TopAppBar)
            DetailHeader(onBackClick = onBackClick)

            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FluxCyan)
                    }
                }
                state.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.error ?: "Error", color = FluxRuby)
                            Button(onClick = { viewModel.retry(postId) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                state.post != null -> {
                    val post = state.post!!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item {
                            PostDetailContent(
                                post = post,
                                isInteractionInFlight = state.isInteractionInFlight,
                                onProfileClick = onProfileClick,
                                onLikeClick = viewModel::onLikeClick,
                                onBookmarkClick = viewModel::onBookmarkClick,
                                onShareClick = viewModel::onShareClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailHeader(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text(
            text = "Post Detail",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
fun PostDetailContent(
    post: PostUiState,
    isInteractionInFlight: Boolean,
    onProfileClick: (Long) -> Unit,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Column {
            // User Node
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onProfileClick(post.authorId) }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.dp, FluxCyan, CircleShape)
                        .background(Color(0xFF1C2128)),
                    contentAlignment = Alignment.Center
                ) {
                    if (post.authorAvatarUrl.isNullOrEmpty()) {
                        Icon(painter = painterResource(id = R.drawable.ic_person), contentDescription = null, tint = Color.Gray)
                    } else {
                        SubcomposeAsyncImage(
                            model = post.authorAvatarUrl,
                            contentDescription = null, 
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = { Box(modifier = Modifier.fillMaxSize().shimmerEffect()) }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "@${post.authorUsername}", fontWeight = FontWeight.Bold, color = FluxCyan)
                    Text(text = post.timestamp, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PostAttachmentsPager(attachments = post.attachments)

            Spacer(modifier = Modifier.height(20.dp))

            // Detailed Caption
            if (!post.caption.isNullOrBlank()) {
                Text(
                    text = post.caption,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Neomorphic Interaction Bar
            DetailedInteractionBar(
                isLiked = post.isLiked,
                isBookmarked = post.isBookmarked,
                likeCount = post.likeCount,
                shareCount = post.shareCount,
                isInteractionInFlight = isInteractionInFlight,
                onLikeClick = onLikeClick,
                onBookmarkClick = onBookmarkClick,
                onShareClick = onShareClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostAttachmentsPager(attachments: List<AttachmentUiState>) {
    if (attachments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(FluxGlassWhite),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No media available", color = Color.White.copy(alpha = 0.7f))
        }
        return
    }

    val orderedAttachments = remember(attachments) { attachments.sortedBy { it.displayOrder } }
    val pagerState = rememberPagerState(pageCount = { orderedAttachments.size })
    val pagerHeight = (LocalConfiguration.current.screenWidthDp.dp * 1.08f).coerceIn(320.dp, 540.dp)

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(pagerHeight)
                .background(Color.Black)
        ) { pageIndex ->
            val attachment = orderedAttachments[pageIndex]
            AttachmentPage(
                attachment = attachment,
                isActivePage = pagerState.currentPage == pageIndex
            )
        }

        if (orderedAttachments.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(orderedAttachments.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 9.dp else 7.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) FluxCyan else Color.White.copy(alpha = 0.35f))
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentPage(
    attachment: AttachmentUiState,
    isActivePage: Boolean
) {
    when (attachment.type) {
        MediaType.VIDEO -> VideoAttachmentPlayer(
            videoUrl = attachment.contentUrl,
            isActivePage = isActivePage
        )
        else -> ImageAttachment(attachment = attachment)
    }
}

@Composable
private fun ImageAttachment(attachment: AttachmentUiState) {
    SubcomposeAsyncImage(
        model = attachment.contentUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        loading = { Box(modifier = Modifier.fillMaxSize().shimmerEffect()) }
    )
}

@Composable
private fun VideoAttachmentPlayer(
    videoUrl: String,
    isActivePage: Boolean
) {
    val context = LocalContext.current
    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
            playWhenReady = false
        }
    }

    LaunchedEffect(isActivePage) {
        exoPlayer.playWhenReady = isActivePage
        if (!isActivePage) {
            exoPlayer.pause()
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = true
                    player = exoPlayer
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
            }
        )
    }
}

@Composable
fun DetailedInteractionBar(
    isLiked: Boolean,
    isBookmarked: Boolean,
    likeCount: Int,
    shareCount: Int,
    isInteractionInFlight: Boolean,
    modifier: Modifier = Modifier,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF1E293B).copy(alpha = 0.2f), RoundedCornerShape(28.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeomorphicIconButton(
            icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            label = if (likeCount > 1000) "${likeCount/1000.0}K" else likeCount.toString(),
            color = if (isLiked) FluxRuby else Color.White.copy(alpha = 0.7f),
            enabled = !isInteractionInFlight,
            onClick = onLikeClick
        )
        
        NeomorphicIconButton(
            icon = if (isBookmarked) Icons.Default.Check else Icons.Default.Add, // Placeholder for Bookmark
            color = if (isBookmarked) FluxCyan else Color.White.copy(alpha = 0.7f),
            enabled = !isInteractionInFlight,
            onClick = onBookmarkClick
        )

        NeomorphicIconButton(
            icon = Icons.Default.Share,
            label = if (shareCount > 1000) "${shareCount / 1000.0}K" else shareCount.toString(),
            color = Color.White.copy(alpha = 0.7f),
            enabled = !isInteractionInFlight,
            onClick = onShareClick
        )
    }
}

@Composable
fun NeomorphicIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String? = null,
    color: Color = Color.White.copy(alpha = 0.7f),
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val contentColor = if (enabled) color else color.copy(alpha = 0.5f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Recessed button effect
        Box(
            modifier = Modifier
                .size(36.dp)
                .shadow(elevation = 2.dp, shape = CircleShape, clip = false, spotColor = Color.Black)
                .background(Color(0xFF161B22), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
        }
        if (label != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.shadow(
                    elevation = if (enabled && color != Color.White.copy(alpha = 0.7f)) 8.dp else 0.dp,
                    spotColor = color
                )
            )
        }
    }
}
