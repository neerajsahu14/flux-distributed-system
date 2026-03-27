package com.neerajsahu.flux.androidclient.feature.relationship.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.neerajsahu.flux.androidclient.R
import com.neerajsahu.flux.androidclient.feature.relationship.domain.model.RelationshipUser

@Composable
fun RelationshipProfileCard(
    user: RelationshipUser,
    buttonText: String,
    onButtonClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProfileClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Image with Neon Glow Border
        Box(
            modifier = Modifier
                .size(64.dp)
                .drawBehind {
                    val glowColor = if (buttonText == "Follow") Color(0xFF00D4FF) else Color(0xFF475569)
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
                        colors = listOf(Color(0xFF00D4FF), Color(0xFFE040FB), Color(0xFF00D4FF))
                    ),
                    shape = CircleShape
                )
                .padding(4.dp)
                .clip(CircleShape)
        ) {
            SubcomposeAsyncImage(
                model = user.profilePicUrl,
                contentDescription = user.username,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF00D4FF),
                            strokeWidth = 2.dp
                        )
                    }
                },
                error = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // User Info (Name and Handle)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.fullName.ifEmpty { user.username.replaceFirstChar { it.uppercase() } },
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "@${user.username.lowercase()}",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp
            )
        }

        // Action Button
        NeonButton(
            text = buttonText,
            onClick = onButtonClick,
            isDark = buttonText == "Following"
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Remove Icon (Disabled as API is pending)
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove",
            tint = Color.White.copy(alpha = 0.2f), // Faded out since API is missing
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFF1E293B).copy(alpha = 0.2f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                .padding(8.dp)
        )
    }
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    isDark: Boolean = false
) {
    val buttonColor = if (isDark) Color(0xFF1E293B) else Color(0xFF00D4FF)

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
    }
}
