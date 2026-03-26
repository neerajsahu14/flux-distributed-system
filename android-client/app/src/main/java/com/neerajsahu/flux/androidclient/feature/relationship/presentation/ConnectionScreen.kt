package com.neerajsahu.flux.androidclient.feature.relationship.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neerajsahu.flux.androidclient.feature.auth.domain.model.User

@Composable
fun ConnectionScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val dummyUsers = remember {
        listOf(
            User(1, "emmajohnson", "emma@example.com", null, "https://api.dicebear.com/7.x/avataaars/svg?seed=Emma"),
            User(2, "jasonmiller", "jason@example.com", null, "https://api.dicebear.com/7.x/avataaars/svg?seed=Jason"),
            User(3, "sarahlee", "sarah@example.com", null, "https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah"),
            User(4, "michaelc", "michael@example.com", null, "https://api.dicebear.com/7.x/avataaars/svg?seed=Michael"),
            User(5, "alexabrooks", "alexa@example.com", null, "https://api.dicebear.com/7.x/avataaars/svg?seed=Alexa"),
            User(6, "danielwong", "daniel@example.com", null, "https://api.dicebear.com/7.x/avataaars/svg?seed=Daniel"),
            User(7, "laurasmith", "laura@example.com", null, "https://api.dicebear.com/7.x/avataaars/svg?seed=Laura")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090E1A))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))

            // Custom Tabs with bottom glow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(48.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConnectionTabItem(
                    text = "Followers",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.height(24.dp))
                ConnectionTabItem(
                    text = "Following",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                placeholder = { Text("Search", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                    focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedBorderColor = Color(0xFF00D4FF).copy(alpha = 0.5f),
                    cursorColor = Color(0xFF00D4FF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Connection List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(dummyUsers, key = { it.id }) { user ->
                    val buttonText = when (user.id.toInt()) {
                        2, 5, 7 -> "Message"
                        4 -> "Following"
                        else -> "Follow"
                    }
                    ProfileCard(
                        user = user,
                        buttonText = buttonText,
                        onButtonClick = { /* TODO */ },
                        onRemoveClick = { /* TODO */ }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionTabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .drawBehind {
                if (isSelected) {
                    val glowColor = Color(0xFF38BDF8)
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.TRANSPARENT
                        setShadowLayer(20.dp.toPx(), 0f, 0f, glowColor.toArgb())
                    }
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(
                            0f, 0f, size.width, size.height,
                            24.dp.toPx(), 24.dp.toPx(),
                            paint
                        )
                    }
                }
            }
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.15f), Color.Transparent)
                    )
                } else {
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                },
                RoundedCornerShape(24.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(Color(0xFF38BDF8), RoundedCornerShape(1.dp))
                )
            }
        }
    }
}
