package com.neerajsahu.flux.androidclient.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = FluxPrimaryDark,
    onPrimary = FluxOnPrimaryDark,
    primaryContainer = FluxPrimaryContainerDark,
    onPrimaryContainer = FluxOnPrimaryContainerDark,

    secondary = FluxSecondaryDark,
    onSecondary = FluxOnSecondaryDark,
    secondaryContainer = FluxSecondaryContainerDark,
    onSecondaryContainer = FluxOnSecondaryContainerDark,

    background = FluxBackgroundDark,
    onBackground = FluxOnBackgroundDark,

    surface = FluxSurfaceDark,
    onSurface = FluxOnSurfaceDark,
    surfaceVariant = FluxSurfaceVariantDark,
    onSurfaceVariant = FluxOnSurfaceVariantDark,

    error = FluxErrorDark,
    onError = FluxOnErrorDark
)

// TODO: Define a proper light palette if a Light Mode is required.
// For now, this serves as a safe fallback mapping.
private val LightColorScheme = lightColorScheme(
    primary = FluxPrimaryDark,
    secondary = FluxSecondaryDark,
    background = FluxOnBackgroundDark,
    surface = FluxOnSurfaceDark,
    onPrimary = FluxOnPrimaryDark,
    onSecondary = FluxOnSecondaryDark,
    onBackground = FluxBackgroundDark,
    onSurface = FluxSurfaceDark
)

@Composable
fun AndroidClientTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set to false by default to enforce the custom "Cosmic Ambient" brand colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Window configuration for Edge-to-Edge UI
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}