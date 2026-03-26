package com.neerajsahu.flux.androidclient.feature.relationship.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neerajsahu.flux.androidclient.feature.auth.domain.model.User

@Composable
fun ProfileCard(
    user: User,
    buttonText: String,
    onButtonClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Image with Neon Glow Border
        Box(
            modifier = Modifier
                .size(64.dp)
                .drawBehind {
                    val glowColor = when (buttonText) {
                        "Message" -> if (user.id == 5L) Color(0xFF9D4EDD) else Color(0xFF475569)
                        "Follow" -> Color(0xFF00D4FF)
                        else -> Color(0xFF475569)
                    }
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(14.dp.toPx(), 0f, 0f, glowColor.copy(alpha = 0.5f).toArgb())
                    }
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawCircle(
                            size.width / 2, size.height / 2,
                            (size.minDimension / 2.1f),
                            paint
                        )
                    }
                }
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(Color(0xFF00D4FF), Color(0xFF9D4EDD), Color(0xFF00D4FF))
                    ),
                    shape = CircleShape
                )
                .padding(4.dp)
                .clip(CircleShape)
        ) {
            AsyncImage(
                model = user.profilePicUrl,
                contentDescription = user.username,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // User Info (Name and Handle)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username.replaceFirstChar { it.uppercase() },
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "@${user.username.lowercase()}",
                color = Color(0xFF94A3B8), // Muted blue-gray for handle
                fontSize = 13.sp
            )
        }

        // Action Button (Follow, Following, Message)
        NeonButton(
            text = buttonText,
            onClick = onButtonClick,
            // Alexa (id 5) has purple button, Jason (id 2) has dark button for "Message" as per image
            isPurple = buttonText == "Message" && user.id == 5L,
            isDark = buttonText == "Following" || (buttonText == "Message" && user.id != 5L)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Remove Icon in circle
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove",
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF1E293B).copy(alpha = 0.4f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                .clickable { onRemoveClick() }
                .padding(8.dp)
        )
    }
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    isPurple: Boolean = false,
    isDark: Boolean = false
) {
    val buttonColor = when {
        isPurple -> Color(0xFF9D4EDD)
        isDark -> Color(0xFF1E293B)
        else -> Color(0xFF00D4FF) // Primary cyan glow
    }

    Box(
        modifier = Modifier
            .width(110.dp)
            .height(38.dp)
            .drawBehind {
                if (!isDark) {
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(10.dp.toPx(), 0f, 0f, buttonColor.copy(alpha = 0.6f).toArgb())
                    }
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(
                            0f, 0f, size.width, size.height,
                            20.dp.toPx(), 20.dp.toPx(),
                            paint
                        )
                    }
                }
            }
            .background(
                if (isDark) buttonColor.copy(alpha = 0.6f) else buttonColor.copy(alpha = 0.85f),
                RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(buttonColor.copy(alpha = 0.9f), buttonColor.copy(alpha = 0.2f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        // Lens flare / gloss effect
        if (!isDark) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.15f)
                            )
                        )
                    )
            )
        }
    }
}
