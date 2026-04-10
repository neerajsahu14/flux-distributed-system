package com.neerajsahu.flux.androidclient.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxLineGradient
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