package com.neerajsahu.flux.androidclient.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.neerajsahu.flux.androidclient.core.navigation.Route
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxCyan
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxGlassBorder
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxGlassWhite

@Composable
fun FluxBottomDock(
    activeRoute: Route,
    onNavigate: (Route) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 8.dp)
            .height(72.dp)
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(36.dp), spotColor = FluxCyan.copy(alpha = 0.5f))
            .background(
                Brush.verticalGradient(
                    colors = listOf(FluxGlassWhite, FluxGlassWhite.copy(alpha = 0.05f))
                ),
                shape = RoundedCornerShape(36.dp)
            )
            .border(1.dp, FluxGlassBorder, RoundedCornerShape(36.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockIcon(
                icon = Icons.Default.Home,
                isSelected = activeRoute is Route.NewsFeed,
                onClick = { onNavigate(Route.NewsFeed) }
            )
            DockIcon(
                icon = Icons.Default.Search,
                isSelected = activeRoute is Route.Explore,
                onClick = { onNavigate(Route.Explore) }
            )
            DockIcon(
                icon = Icons.Default.Add,
                isSelected = activeRoute is Route.CreatePost,
                isCenter = true,
                onClick = { onNavigate(Route.CreatePost) }
            )
            DockIcon(
                icon = Icons.Default.Notifications,
                isSelected = activeRoute is Route.Notifications,
                onClick = { onNavigate(Route.Notifications) }
            )
            DockIcon(
                icon = Icons.Default.Person,
                isSelected = activeRoute is Route.Profile,
                onClick = { onNavigate(Route.Profile) }
            )
        }
    }
}

@Composable
fun DockIcon(
    icon: ImageVector,
    isSelected: Boolean,
    isCenter: Boolean = false,
    onClick: () -> Unit
) {
    val density = LocalDensity.current

    // 1. Cache the neon glow paint to prevent GC overhead during recomposition
    val glowPaint = remember(density) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.TRANSPARENT
            // Soft omnidirectional glow: 16dp spread, 60% opacity
            setShadowLayer(
                with(density) { 16.dp.toPx() },
                0f,
                0f,
                FluxCyan.copy(alpha = 0.6f).toArgb()
            )
        }
    }

    Box(
        modifier = Modifier
            .size(if (isCenter) 56.dp else 48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier
                        .drawBehind {
                            // 2. Draw the custom neon glow exactly behind the center
                            // Radius is slightly smaller than the box to allow the blur to spread inward & outward
                            val radius = size.width / 2.5f
                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawCircle(
                                    center.x,
                                    center.y,
                                    radius,
                                    glowPaint
                                )
                            }
                        }
                        .background(
                            Brush.radialGradient(
                                // 3. Toned down center fill from 0.15f to 0.10f for a glassier look
                                colors = listOf(FluxCyan.copy(alpha = 0.10f), Color.Transparent)
                            )
                        )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) FluxCyan else Color.Gray,
            modifier = Modifier.size(if (isCenter) 32.dp else 24.dp)
        )
    }
}
