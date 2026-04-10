package com.neerajsahu.flux.androidclient.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxGlassBorder
import com.neerajsahu.flux.androidclient.core.ui.theme.FluxGlassWhite

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
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
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}