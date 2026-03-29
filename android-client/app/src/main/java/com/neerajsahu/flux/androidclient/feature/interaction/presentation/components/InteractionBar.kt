package com.neerajsahu.flux.androidclient.feature.interaction.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.neerajsahu.flux.androidclient.R
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
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Like Action
        InteractionItem(
            iconResId = if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_outline,
            tint = if (isLiked) FluxRuby else Color.Gray,
            countText = likeCount.toString(),
            onClick = onLikeClick,
            enabled = !isInteractionInFlight
        )

        // Share Action
        InteractionItem(
            iconResId = R.drawable.ic_share,
            tint = if (isInteractionInFlight) Color.Gray else Color.White,
            countText = shareCount?.toString() ?: "0",
            onClick = onShareClick,
            enabled = !isInteractionInFlight
        )

        // Bookmark Action
        InteractionItem(
            iconResId = if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline,
            tint = if (isBookmarked) FluxCyan else Color.Gray,
            countText = if (isBookmarked) "Saved" else "Save",
            onClick = onBookmarkClick,
            enabled = !isInteractionInFlight
        )
    }
}

@Composable
fun InteractionItem(
    iconResId: Int,
    tint: Color,
    countText: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = countText,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
