package com.bubble.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color schema and token palettes for Free Core and Pro Premium themes.
 */
data class BubbleColorScheme(
    val surfaceOverlay: Color,
    val surfaceOverlayVariant: Color,
    val onSurfaceOverlay: Color,
    val onSurfaceOverlaySubtle: Color,
    val borderStroke: Color,
    val unreadBadgeBg: Color,
    val unreadBadgeText: Color,
    val appBadgeBackground: Color,
    val dismissTargetBg: Color,
    val dismissTargetActiveBg: Color,
    val dismissTargetIcon: Color,
    val actionButtonBg: Color,
    val actionButtonText: Color,
    val isGlassmorphism: Boolean = false,
    val blurRadius: Float = 0f
)

val FreeDarkColorScheme = BubbleColorScheme(
    surfaceOverlay = Color(0xFF1E1E24),
    surfaceOverlayVariant = Color(0xFF282832),
    onSurfaceOverlay = Color(0xFFFFFFFF),
    onSurfaceOverlaySubtle = Color(0xFFA0A0B0),
    borderStroke = Color(0x33FFFFFF), // 20% White stroke
    unreadBadgeBg = Color(0xFFFF3B30),
    unreadBadgeText = Color(0xFFFFFFFF),
    appBadgeBackground = Color(0xFF1E1E24),
    dismissTargetBg = Color(0xCC202025),
    dismissTargetActiveBg = Color(0xFFFF3B30),
    dismissTargetIcon = Color(0xFFFFFFFF),
    actionButtonBg = Color(0xFF2E2E3A),
    actionButtonText = Color(0xFFFFFFFF)
)

val FreeLightColorScheme = BubbleColorScheme(
    surfaceOverlay = Color(0xFFFFFFFF),
    surfaceOverlayVariant = Color(0xFFF4F4F6),
    onSurfaceOverlay = Color(0xFF1A1A1E),
    onSurfaceOverlaySubtle = Color(0xFF6B6B78),
    borderStroke = Color(0x1F000000), // 12% Black stroke
    unreadBadgeBg = Color(0xFFD32F2F),
    unreadBadgeText = Color(0xFFFFFFFF),
    appBadgeBackground = Color(0xFFFFFFFF),
    dismissTargetBg = Color(0xCC303035),
    dismissTargetActiveBg = Color(0xFFE53935),
    dismissTargetIcon = Color(0xFFFFFFFF),
    actionButtonBg = Color(0xFFECECEF),
    actionButtonText = Color(0xFF1A1A1E)
)

val ProOledBlackColorScheme = BubbleColorScheme(
    surfaceOverlay = Color(0xFF000000),
    surfaceOverlayVariant = Color(0xFF101012),
    onSurfaceOverlay = Color(0xFFFFFFFF),
    onSurfaceOverlaySubtle = Color(0xFF888892),
    borderStroke = Color(0xFF2C2C32),
    unreadBadgeBg = Color(0xFFFF2A42),
    unreadBadgeText = Color(0xFFFFFFFF),
    appBadgeBackground = Color(0xFF000000),
    dismissTargetBg = Color(0xFF161618),
    dismissTargetActiveBg = Color(0xFFFF1744),
    dismissTargetIcon = Color(0xFFFFFFFF),
    actionButtonBg = Color(0xFF1A1A1E),
    actionButtonText = Color(0xFFFFFFFF)
)

val ProFrostedGlassColorScheme = BubbleColorScheme(
    surfaceOverlay = Color(0x5A1A1B24),
    surfaceOverlayVariant = Color(0x70252735),
    onSurfaceOverlay = Color(0xFFFFFFFF),
    onSurfaceOverlaySubtle = Color(0xFFC0C2D0),
    borderStroke = Color(0x40FFFFFF),
    unreadBadgeBg = Color(0xFFFF3B30),
    unreadBadgeText = Color(0xFFFFFFFF),
    appBadgeBackground = Color(0x801A1B24),
    dismissTargetBg = Color(0x60000000),
    dismissTargetActiveBg = Color(0xE0FF3B30),
    dismissTargetIcon = Color(0xFFFFFFFF),
    actionButtonBg = Color(0x40FFFFFF),
    actionButtonText = Color(0xFFFFFFFF),
    isGlassmorphism = true,
    blurRadius = 24f
)

val ProCyberpunkColorScheme = BubbleColorScheme(
    surfaceOverlay = Color(0xFF0B0D17),
    surfaceOverlayVariant = Color(0xFF15192C),
    onSurfaceOverlay = Color(0xFF00F0FF),
    onSurfaceOverlaySubtle = Color(0xFF7B88A8),
    borderStroke = Color(0xFF00F0FF),
    unreadBadgeBg = Color(0xFFFF007F),
    unreadBadgeText = Color(0xFFFFFFFF),
    appBadgeBackground = Color(0xFF0B0D17),
    dismissTargetBg = Color(0xCC0B0D17),
    dismissTargetActiveBg = Color(0xFFFF007F),
    dismissTargetIcon = Color(0xFF00F0FF),
    actionButtonBg = Color(0xFF1D233D),
    actionButtonText = Color(0xFF00F0FF)
)
