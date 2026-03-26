package com.neerajsahu.flux.androidclient.feature.auth.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neerajsahu.flux.androidclient.R

@Composable
fun AuthScreenContainer(
    modifier: Modifier = Modifier,
    scrollState: ScrollState? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Background Image with low opacity
        Image(
            painter = painterResource(id = R.drawable.auth_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = .5f
        )
        
        // Dark overlay for extra contrast and depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color(0xFF26395E).copy(alpha = 0.8f)
                        )
                    )
                )
        )

        BackgroundNetwork(modifier = Modifier.fillMaxSize())

        val columnModifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .let { if (scrollState != null) it.verticalScroll(scrollState) else it }

        Column(
            modifier = columnModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun FluxLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 80.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(iconSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 2.dp.toPx()
                val color = Color(0xFF38BDF8)
                
                drawCircle(
                    color = color,
                    radius = size.minDimension / 2.5f,
                    style = Stroke(width = strokeWidth)
                )
                
                val path1 = Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(size.width * 0.2f, 0f, size.width * 0.8f, size.height))
                }
                val path2 = Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(0f, size.height * 0.2f, size.width, size.height * 0.8f))
                }
                
                drawPath(path1, color, style = Stroke(width = strokeWidth))
                drawPath(path2, color, style = Stroke(width = strokeWidth))
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "FLUX",
            style = TextStyle(
                color = Color(0xFF38BDF8),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
        )
    }
}

@Composable
fun FluxInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconResId: Int,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 14.sp
                )
            )
            trailingContent?.invoke()
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .drawBehind {
                    val shadowColor = Color(0xFF38BDF8).copy(alpha = 0.4f).toArgb()
                    val transparentColor = Color.Transparent.toArgb()
                    val paint = android.graphics.Paint().apply {
                        color = transparentColor
                        setShadowLayer(16.dp.toPx(), 0f, 0f, shadowColor)
                    }
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(
                            0f, 0f, size.width, size.height,
                            28.dp.toPx(), 28.dp.toPx(),
                            paint
                        )
                    }
                }
                .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF38BDF8).copy(alpha = 0.8f),
                            Color(0xFF38BDF8).copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                    visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(color = Color.Gray.copy(alpha = 0.5f), fontSize = 16.sp)
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}

@Composable
fun FluxButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF38BDF8),
                        Color(0xFF6366F1)
                    )
                )
            )
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Text(
                text = text,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.25.sp
                )
            )
        }
    }
}

@Composable
fun BackgroundNetwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 0.5.dp.toPx()
        val color = Color(0xFF334155).copy(alpha = 0.3f)
        
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.2f),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.4f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.2f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, size.height * 0.7f),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.5f),
            strokeWidth = strokeWidth
        )
    }
}
