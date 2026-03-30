package com.neerajsahu.flux.androidclient.feature.feed.presentation.component

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.neerajsahu.flux.androidclient.core.ui.components.shimmerEffect
import coil.compose.SubcomposeAsyncImage
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxCyan
import com.neerajsahu.flux.androidclient.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// ─── Design tokens (shared with CreatePostScreen) ──────────────────────────────
private val FluxSurface   = Color(0xFF0A0E1A)
private val FluxCard      = Color(0xFF0F1629)
private val FluxBorder    = Color(0xFF1E2A45)
private val FluxMuted     = Color(0xFF3A4A6A)
private val FluxText      = Color(0xFFE8EEF8)
private val FluxTextDim   = Color(0xFF6B7FA8)

// ─── Public API ────────────────────────────────────────────────────────────────

/**
 * Drop-in media preview card for CreatePostScreen.
 *
 * @param uri           The media URI (image or video)
 * @param isVideo       True if [uri] points to a video
 * @param onRemove      Called when the user taps the ✕ button
 * @param onReplace     Called when the user taps the swap area (optional)
 * @param modifier      Passed through to the root Box
 */
@Composable
fun MediaPreviewCard(
    uri: Uri,
    isVideo: Boolean,
    onRemove: () -> Unit,
    onReplace: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Entry animation
    val entryScale = remember { Animatable(0.92f) }
    val entryAlpha = remember { Animatable(0f) }
    LaunchedEffect(uri) {
        launch { entryScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
        launch { entryAlpha.animateTo(1f, tween(220)) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(entryScale.value)
            .drawBehind {
                // Cyan glow halo behind the card
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(FluxCyan.copy(alpha = 0.10f), Color.Transparent),
                        radius = size.maxDimension * 0.65f,
                        center = Offset(size.width / 2, size.height / 2)
                    ),
                    radius = size.maxDimension * 0.65f
                )
            }
    ) {
        // ── Card shell ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(FluxCyan.copy(0.45f), FluxBorder, FluxBorder, FluxBorder)
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .background(FluxCard)
                .aspectRatio(if (isVideo) 16f / 9f else 4f / 3f)
        ) {
            if (isVideo) {
                VideoPreview(uri = uri)
            } else {
                ImagePreview(uri = uri, onReplace = onReplace)
            }

            // Bottom type badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isVideo) "VIDEO" else "IMAGE",
                    color = FluxCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.W700,
                    letterSpacing = 1.8.sp
                )
            }
        }

        // ── X dismiss button — floats top-end, overlapping card border ──
        DismissButton(
            onRemove = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 10.dp, y = (-10).dp)   // deliberately bleeds outside card
        )
    }
}

// ─── Image preview ─────────────────────────────────────────────────────────────

@Composable
private fun ImagePreview(uri: Uri, onReplace: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onReplace
            )
    ) {
        SubcomposeAsyncImage(
            model = uri,
            contentDescription = "Media preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = { Box(modifier = Modifier.fillMaxSize().shimmerEffect()) }
        )

        // Subtle top-to-bottom gradient for depth
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(0.25f), Color.Transparent)
                    )
                )
        )
    }
}

// ─── Video preview ─────────────────────────────────────────────────────────────

@Composable
private fun VideoPreview(uri: Uri) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var currentMs by remember { mutableLongStateOf(0L) }
    var controlsVisible by remember { mutableStateOf(true) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = false
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    // Sync playback state → compose state
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Poll position for scrubber
    LaunchedEffect(exoPlayer) {
        while (isActive) {
            val dur = exoPlayer.duration.takeIf { it > 0 } ?: 0L
            val pos = exoPlayer.currentPosition
            durationMs = dur
            currentMs = pos
            progress = if (dur > 0) pos.toFloat() / dur else 0f
            delay(200)
        }
    }

    // Auto-hide controls
    LaunchedEffect(isPlaying, controlsVisible) {
        if (isPlaying && controlsVisible) {
            delay(3000)
            controlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { controlsVisible = !controlsVisible }
    ) {
        // ExoPlayer surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false   // We draw our own controls
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Overlay controls ──────────────────────────────────────
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.30f))
                )

                // Centre play / pause button
                val playScale by animateFloatAsState(
                    targetValue = if (isPlaying) 0.95f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "play_scale"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .scale(playScale)
                        .size(60.dp)
                        .drawBehind {
                            // Glowing ring
                            drawCircle(
                                color = FluxCyan.copy(alpha = 0.20f),
                                radius = size.minDimension / 2f
                            )
                            drawCircle(
                                color = FluxCyan.copy(alpha = 0.70f),
                                radius = size.minDimension / 2f,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play_arrow),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Bottom: time + scrubber + mute
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(0.70f))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Scrubber
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp)),
                        color = FluxCyan,
                        trackColor = Color.White.copy(alpha = 0.20f),
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Timestamp
                        Text(
                            text = "${currentMs.toTimestamp()} / ${durationMs.toTimestamp()}",
                            color = FluxTextDim,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.W500,
                            letterSpacing = 0.5.sp
                        )

                        // Mute toggle
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(0.40f))
                                .border(1.dp, FluxBorder, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isMuted = !isMuted
                                    exoPlayer.volume = if (isMuted) 0f else 1f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(if (isMuted) R.drawable.volume_off else R.drawable.volume_up),
                                contentDescription = null,
                                tint = FluxText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Dismiss (X) button ────────────────────────────────────────────────────────

@Composable
private fun DismissButton(
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(150)
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
    }

    val pressScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dismiss_press"
    )

    Box(
        modifier = modifier
            .scale(scale.value * pressScale)
            .size(36.dp)
            .drawBehind {
                // Glow ring behind button
                drawCircle(
                    color = Color(0xFFFF4466).copy(alpha = 0.25f),
                    radius = size.minDimension / 2f + 4.dp.toPx()
                )
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF2A1020), Color(0xFF1A0A14))
                )
            )
            .border(1.dp, Color(0xFFFF4466).copy(0.55f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onRemove
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove media",
            tint = Color(0xFFFF6680),
            modifier = Modifier.size(16.dp)
        )
    }
}

// ─── Helpers ───────────────────────────────────────────────────────────────────

private fun Long.toTimestamp(): String {
    val totalSec = this / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}