package com.neerajsahu.flux.androidclient.core.ui.components
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val startOffsetX by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000)
        ),
        label = "shimmer_offsetX"
    )
    onGloballyPositioned { size = it.size }
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1A1A1A), // very dark gray
                    Color(0xFF424242), // lighter dark gray
                    Color(0xFF1A1A1A),
                ),
                start = Offset(if (size.width.toFloat() > 0) startOffsetX else 0f, 0f),
                end = Offset(if (size.width.toFloat() > 0) startOffsetX + size.width.toFloat() else 0f, if (size.height.toFloat() > 0) size.height.toFloat() else 0f)
            )
        )
}
