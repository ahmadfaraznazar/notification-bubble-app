package com.bubble.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalBubbleColorScheme = staticCompositionLocalOf { FreeDarkColorScheme }

enum class ThemePreset {
    SYSTEM_DYNAMIC,
    STANDARD_DARK,
    STANDARD_LIGHT,
    OLED_BLACK,
    FROSTED_GLASS,
    CYBERPUNK_NEON
}

/**
 * Bubble Design System theme provider.
 */
@Composable
fun BubbleTheme(
    preset: ThemePreset = ThemePreset.SYSTEM_DYNAMIC,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val bubbleColors = when (preset) {
        ThemePreset.SYSTEM_DYNAMIC -> {
            if (isDarkTheme) FreeDarkColorScheme else FreeLightColorScheme
        }
        ThemePreset.STANDARD_DARK -> FreeDarkColorScheme
        ThemePreset.STANDARD_LIGHT -> FreeLightColorScheme
        ThemePreset.OLED_BLACK -> ProOledBlackColorScheme
        ThemePreset.FROSTED_GLASS -> ProFrostedGlassColorScheme
        ThemePreset.CYBERPUNK_NEON -> ProCyberpunkColorScheme
    }

    val materialColors = when {
        preset == ThemePreset.SYSTEM_DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme || preset == ThemePreset.OLED_BLACK || preset == ThemePreset.CYBERPUNK_NEON -> {
            darkColorScheme()
        }
        else -> {
            lightColorScheme()
        }
    }

    CompositionLocalProvider(
        LocalBubbleColorScheme provides bubbleColors
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = BubbleTypography,
            content = content
        )
    }
}

object BubbleTheme {
    val colors: BubbleColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalBubbleColorScheme.current

    val dimensions: BubbleDimensions
        get() = BubbleDimensions

    val typography: androidx.compose.material3.Typography
        get() = BubbleTypography
}
