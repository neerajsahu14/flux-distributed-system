package com.neerajsahu.flux.androidclient.feature.feed.presentation

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxBackgroundDark
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxCyan
import com.neerajsahu.flux.androidclient.feature.feed.presentation.component.MediaPreviewCard
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// Flux design tokens
private val FluxSurface = Color(0xFF0A0E1A)
private val FluxCard = Color(0xFF0F1629)
private val FluxBorder = Color(0xFF1E2A45)
private val FluxMuted = Color(0xFF3A4A6A)
private val FluxText = Color(0xFFE8EEF8)
private val FluxTextDim = Color(0xFF6B7FA8)

@Composable
fun CreatePostScreen(
    onBackClick: () -> Unit,
    onPostCreated: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> viewModel.onMediaSelected(uri?.toString()) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) { viewModel.consumeSuccess(); onPostCreated() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FluxSurface)
    ) {
        // Ambient glow blobs in background
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-60).dp, y = 200.dp)
                .background(
                    Brush.radialGradient(
                        listOf(FluxCyan.copy(alpha = 0.06f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        listOf(FluxCyan.copy(alpha = 0.04f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, FluxBorder, CircleShape)
                        .background(FluxCard)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = FluxText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                // Minimal wordmark label
                Text(
                    text = "NEW POST",
                    color = FluxTextDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = 3.sp
                )

                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(40.dp)) // balance
            }

            // ── Hero media zone ──────────────────────────────────────
            val previewUri = state.selectedMediaUri?.let(Uri::parse)
            val isVideo = previewUri != null && context.isVideoUri(previewUri)


            if (previewUri != null) {
                MediaPreviewCard(
                    uri = previewUri,
                    isVideo = isVideo,
                    onRemove = viewModel::clearSelectedMedia,
                    onReplace = {
                        mediaPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            } else {
                    // Empty state — editorial dashed grid
                    EmptyMediaState(
                        onClick = {
                            mediaPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        }
                    )
                }

            Spacer(Modifier.height(20.dp))

            // ── Caption field ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "CAPTION",
                    color = FluxTextDim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = 2.5.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, FluxBorder, RoundedCornerShape(16.dp))
                        .background(FluxCard)
                        .padding(16.dp)
                ) {
                    if (state.caption.isEmpty()) {
                        Text(
                            text = "say something about this moment...",
                            color = FluxMuted,
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    BasicTextField(
                        value = state.caption,
                        onValueChange = viewModel::onCaptionChanged,
                        textStyle = TextStyle(
                            color = FluxText,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        ),
                        cursorBrush = SolidColor(FluxCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 80.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Post button ───────────────────────────────────────────
            PostButton(
                isEnabled = !state.isSubmitting && state.selectedMediaUri != null,
                isLoading = state.isSubmitting,
                onClick = {
                    val uri = state.selectedMediaUri?.let(Uri::parse)
                    val mediaPart = uri?.let { context.toMediaMultipartPart(it) }
                    if (mediaPart != null) viewModel.createPost(mediaPart)
                    else viewModel.setError("Unable to read selected media.")
                }
            )

            Spacer(Modifier.navigationBarsPadding())
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
        )
}

}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyMediaState(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Dashed cross-hair grid
                val dash = 6.dp.toPx()
                val gap = 8.dp.toPx()
                val step = dash + gap
                val cols = (size.width / step).toInt()
                val rows = (size.height / step).toInt()

                for (col in 0..cols) {
                    val x = col * step
                    var y = 0f
                    while (y < size.height) {
                        drawLine(
                            color = Color(0xFF1E2A45),
                            start = Offset(x, y),
                            end = Offset(x, (y + dash).coerceAtMost(size.height)),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += step
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Glowing circle ring
        Box(
            modifier = Modifier
                .size(72.dp)
                .drawBehind {
                    drawCircle(
                        color = FluxCyan.copy(alpha = 0.15f),
                        radius = size.minDimension / 2f
                    )
                    drawCircle(
                        color = FluxCyan.copy(alpha = 0.6f),
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = FluxCyan,
                fontSize = 28.sp,
                fontWeight = FontWeight.W300,
                lineHeight = 28.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "tap to add photo or video",
            color = FluxTextDim,
            fontSize = 13.sp,
            letterSpacing = 0.3.sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "jpg · png · mp4",
            color = FluxMuted.copy(alpha = 0.6f),
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
    }
}

// ── Post button ────────────────────────────────────────────────────────────────

@Composable
private fun PostButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.97f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .scale(scale)
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (isEnabled)
                    Brush.horizontalGradient(
                        listOf(FluxCyan, FluxCyan.copy(green = 0.95f, blue = 0.75f))
                    )
                else
                    Brush.horizontalGradient(
                        listOf(FluxMuted.copy(alpha = 0.3f), FluxMuted.copy(alpha = 0.2f))
                    )
            )
            .clickable(enabled = isEnabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Subtle inner sheen
        if (isEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.Black,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "PUBLISH",
                    color = if (isEnabled) Color.Black else FluxTextDim,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W700,
                    letterSpacing = 3.sp
                )
            }
        }
    }
}

// ── Helpers (unchanged) ────────────────────────────────────────────────────────

private fun Context.toMediaMultipartPart(uri: Uri): MultipartBody.Part? {
    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    val extension = when {
        mimeType.startsWith("image") -> "jpg"
        mimeType.startsWith("video") -> "mp4"
        else -> "bin"
    }
    val fileName = queryFileName(uri) ?: "post_${System.currentTimeMillis()}.$extension"
    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("image", fileName, requestBody)
}

private fun Context.isVideoUri(uri: Uri): Boolean =
    contentResolver.getType(uri)?.startsWith("video") == true

private fun Context.queryFileName(uri: Uri): String? {
    val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx != -1 && it.moveToFirst()) return it.getString(idx)
    }
    return null
}