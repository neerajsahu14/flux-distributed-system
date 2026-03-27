package com.neerajsahu.flux.androidclient.core.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.neerajsahu.flux.androidclient.core.ui.theme.*
import kotlin.math.sin

@Composable
fun FluxLineBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluxLine")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val centerX = width / 2

        val path = Path().apply {
            moveTo(centerX, 0f)
            for (y in 0..height.toInt() step 5) {
                val xOffset = sin(y.toFloat() * 0.004f + phase) * 50f
                lineTo(centerX + xOffset, y.toFloat())
            }
        }

        // Deep glow
        drawPath(
            path = path,
            brush = Brush.verticalGradient(FluxLineGradient),
            style = Stroke(width = 20f, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            alpha = 0.1f
        )
        
        // Mid glow
        drawPath(
            path = path,
            brush = Brush.verticalGradient(FluxLineGradient),
            style = Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            alpha = 0.3f
        )

        // Core line
        drawPath(
            path = path,
            brush = Brush.verticalGradient(FluxLineGradient),
            style = Stroke(width = 2f, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            alpha = 0.8f
        )
    }
}

@Composable
fun FluxStrand(
    startOffset: Offset,
    endOffset: Offset,
    alpha: Float = 0.3f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path().apply {
            moveTo(startOffset.x, startOffset.y)
            // Quadratic Bezier for a curved strand
            val controlPoint = Offset(
                x = (startOffset.x + endOffset.x) / 2 + 20f,
                y = (startOffset.y + endOffset.y) / 2
            )
            quadraticTo(controlPoint.x, controlPoint.y, endOffset.x, endOffset.y)
        }
        drawPath(
            path = path,
            color = FluxCyan,
            style = Stroke(width = 1f),
            alpha = alpha
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(FluxGlassWhite, FluxGlassWhite.copy(alpha = 0.05f))
                )
            )
            .border(0.5.dp, FluxGlassBorder, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
