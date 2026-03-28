package com.neerajsahu.flux.androidclient.feature.feed.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.neerajsahu.flux.androidclient.feature.interaction.presentation.components.InteractionBar

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = state.error ?: "Failed to load post",
                            modifier = Modifier.padding(16.dp)
                        )
                        TextButton(onClick = { viewModel.retry(postId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            state.post != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        val post = state.post!!

                        // Main image/video
                        if (post.contentUrl != null) {
                            AsyncImage(
                                model = post.imageUrl,
                                contentDescription = post.caption,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )
                        }
                    }

                    item {
                        val post = state.post!!

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Author info
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onProfileClick(post.author.id) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!post.author.profilePicUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = post.author.profilePicUrl,
                                        contentDescription = "Author avatar",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .clickable { onProfileClick(post.author.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👤")
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp)
                                ) {
                                    Text(
                                        text = "@${post.author.username}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = post.createdAt,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Caption
                            if (!post.caption.isNullOrBlank()) {
                                Text(
                                    text = post.caption,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Engagement stats
                            InteractionBar(
                                isLiked = post.isLiked,
                                isBookmarked = post.isBookmarked,
                                likeCount = post.likeCount,
                                shareCount = post.shareCount,
                                isInteractionInFlight = state.isInteractionInFlight,
                                onLikeClick = viewModel::onLikeClick,
                                onBookmarkClick = viewModel::onBookmarkClick,
                                onShareClick = viewModel::onShareClick
                            )
                        }
                    }

                    // Attachments carousel
                    if (state.post!!.attachments.isNotEmpty()) {
                        item {
                            Column {
                                Text(
                                    text = "Media",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(state.post!!.attachments) { attachment ->
                                        ElevatedCard(
                                            modifier = Modifier
                                                .fillMaxWidth(0.8f)
                                                .height(200.dp)
                                        ) {
                                            AsyncImage(
                                                model = attachment.thumbnailUrl
                                                    ?: attachment.contentUrl,
                                                contentDescription = "Attachment",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

