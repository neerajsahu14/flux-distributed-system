package com.neerajsahu.flux.androidclient.feature.feed.data.mapper

import com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto.AttachmentResponseDto
import com.neerajsahu.flux.androidclient.feature.feed.data.remote.dto.PostDetailResponseDto
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.Attachment
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.MediaType
import com.neerajsahu.flux.androidclient.feature.feed.domain.model.PostDetail
import com.neerajsahu.flux.androidclient.feature.feed.presentation.model.AttachmentUiState
import com.neerajsahu.flux.androidclient.feature.feed.presentation.model.PostUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun PostDetailResponseDto.toUiState(): PostUiState {
    val orderedAttachments = this.attachments
        .sortedBy { it.displayOrder }
        .map { it.toUiState() }

    return PostUiState(
        id = this.id,
        authorName = this.author.username.replaceFirstChar { it.uppercase() },
        authorUsername = this.author.username,
        authorAvatarUrl = this.author.profilePicUrl,
        timestamp = formatFeedDateTime(this.createdAt),
        caption = this.caption,
        mainMediaType = mapMediaType(this.mediaType),
        mainThumbnailUrl = this.imageUrl,
        mainContentUrl = this.contentUrl,
        likeCount = this.likeCount,
        shareCount = this.shareCount,
        isLiked = this.isLiked,
        isBookmarked = this.isBookmarked,
        attachments = orderedAttachments,
        authorId = author.id
    )
}

fun PostDetail.toUiState(): PostUiState {
    val orderedAttachments = this.attachments
        .sortedBy { it.displayOrder }
        .map { it.toUiState() }

    return PostUiState(
        id = this.id,
        authorName = this.author.username.replaceFirstChar { it.uppercase() },
        authorUsername = this.author.username,
        authorAvatarUrl = this.author.profilePicUrl,
        timestamp = formatFeedDateTime(this.createdAt),
        caption = this.caption,
        mainMediaType = mapMediaType(this.mediaType),
        mainThumbnailUrl = this.imageUrl,
        mainContentUrl = this.contentUrl,
        likeCount = this.likeCount,
        shareCount = this.shareCount,
        isLiked = this.isLiked,
        isBookmarked = this.isBookmarked,
        attachments = orderedAttachments,
        authorId = author.id
    )
}

private fun AttachmentResponseDto.toUiState(): AttachmentUiState {
    return AttachmentUiState(
        id = this.id,
        type = mapMediaType(this.mediaType),
        thumbnailUrl = this.thumbnailUrl,
        contentUrl = this.contentUrl,
        displayOrder = this.displayOrder
    )
}

private fun Attachment.toUiState(): AttachmentUiState {
    return AttachmentUiState(
        id = this.id,
        type = mapMediaType(this.mediaType),
        thumbnailUrl = this.thumbnailUrl,
        contentUrl = this.contentUrl,
        displayOrder = this.displayOrder
    )
}

private fun mapMediaType(type: String): MediaType {
    val normalized = type.trim().uppercase()
    return try {
        when {
            normalized.contains("VIDEO") -> MediaType.VIDEO
            normalized.contains("IMAGE") -> MediaType.IMAGE
            else -> MediaType.valueOf(normalized)
        }
    } catch (e: IllegalArgumentException) {
        MediaType.UNKNOWN
    }
}

fun formatFeedDateTime(isoString: String): String {
    return try {
        val past = Instant.parse(isoString)
        val now = Instant.now()
        
        val minutes = ChronoUnit.MINUTES.between(past, now)
        val hours = ChronoUnit.HOURS.between(past, now)
        val days = ChronoUnit.DAYS.between(past, now)

        when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MMM dd").withZone(ZoneId.systemDefault())
                formatter.format(past)
            }
        }
    } catch (e: Exception) {
        ""
    }
}
