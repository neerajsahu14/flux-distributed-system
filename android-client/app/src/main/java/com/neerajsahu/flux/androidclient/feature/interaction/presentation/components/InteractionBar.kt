package com.neerajsahu.flux.androidclient.feature.interaction.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxCyan
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxRuby

@Composable
fun InteractionBar(
    isLiked: Boolean,
    isBookmarked: Boolean,
    likeCount: Int,
    shareCount: Int? = null,
    isInteractionInFlight: Boolean,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onLikeClick, enabled = !isInteractionInFlight) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) FluxRuby else Color.Gray
                )
            }
            Text(
                text = likeCount.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onBookmarkClick, enabled = !isInteractionInFlight) {
                Text(
                    text = if (isBookmarked) "Saved" else "Save",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isBookmarked) FluxCyan else Color.Gray
                )
            }

            IconButton(onClick = onShareClick, enabled = !isInteractionInFlight) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share",
                    tint = if (isInteractionInFlight) Color.Gray else Color.White
                )
            }

            if (shareCount != null) {
                Text(
                    text = shareCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }
    }
}


