package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

private val DarkColorScheme = darkColorScheme(
    primary = LogoBlueDark,
    secondary = SecondaryGreenDark,
    background = BackgroundDark,
    surface = SurfaceContainerLowestDark,
    surfaceVariant = SurfaceContainerDark,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFC1C6D3)
)

private val LightColorScheme = lightColorScheme(
    primary = LogoBlue,
    onPrimary = Color.White,
    secondary = SecondaryGreen,
    onSecondary = Color.White,
    tertiary = WarmTertiary,
    onTertiary = Color.White,
    background = BackgroundLight,
    surface = Color.White,
    surfaceVariant = SurfaceContainerLow,
    onBackground = TextOnSurface,
    onSurface = TextOnSurface,
    onSurfaceVariant = TextOnSurfaceVariant,
    outlineVariant = OutlineVariantLight,
    outline = OutlineLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
    // Allow turning off dynamic color to maintain design fidelity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
