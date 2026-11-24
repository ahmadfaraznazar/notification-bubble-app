package com.bubble.ui.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class MascotMood {
    WAVING,
    EXPLAINING,
    CELEBRATING
}

/**
 * Pure Jetpack Compose vector illustration of "Bubby" — the cheerful bouncing jelly mascot.
 * Renders zero-asset canvas animations with expressive emotional states across onboarding steps.
 */
@Composable
fun MascotIllustration(
    mood: MascotMood,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MascotFloat")

    // Gentle vertical bobbing motion
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MascotBob"
    )

    // Subtle breathing scale
    val breathingSquash by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MascotBreathing"
    )

    val moodDescription = when (mood) {
        MascotMood.WAVING -> "Bubby the bubble mascot waving happily"
        MascotMood.EXPLAINING -> "Bubby the bubble mascot explaining permissions"
        MascotMood.CELEBRATING -> "Bubby the bubble mascot celebrating complete setup"
    }

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = moodDescription },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .offset { IntOffset(0, floatOffset.roundToInt()) }
        ) {
            val width = size.toPx()
            val height = size.toPx()
            val centerX = width / 2f
            val centerY = height / 2f
            val radius = (width.coerceAtMost(height) / 2f) * 0.72f * breathingSquash

            // 1. Soft Shadow
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x330284C7), Color.Transparent),
                    center = Offset(centerX, centerY + radius + 14f),
                    radius = radius * 0.9f
                ),
                topLeft = Offset(centerX - radius * 0.8f, centerY + radius),
                size = Size(radius * 1.6f, 16f)
            )

            // 2. Jelly Bubble Body (Cyan to Sky-Blue Gradient)
            val bodyBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF38BDF8), // Sky-400
                    Color(0xFF0284C7), // Sky-600
                    Color(0xFF0369A1)  // Sky-700
                ),
                startY = centerY - radius,
                endY = centerY + radius
            )

            drawCircle(
                brush = bodyBrush,
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // 3. Glossy Specular Highlight (Top-Left)
            drawOval(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0x99FFFFFF), Color(0x00FFFFFF)),
                    start = Offset(centerX - radius * 0.5f, centerY - radius * 0.6f),
                    end = Offset(centerX, centerY)
                ),
                topLeft = Offset(centerX - radius * 0.55f, centerY - radius * 0.65f),
                size = Size(radius * 0.55f, radius * 0.35f)
            )

            // 4. Eyes & Facial Expressions
            drawMascotFace(
                mood = mood,
                centerX = centerX,
                centerY = centerY,
                radius = radius
            )

            // 5. Mascot Accessories based on Mood
            when (mood) {
                MascotMood.WAVING -> drawWavingHand(centerX, centerY, radius)
                MascotMood.EXPLAINING -> drawPointingBadge(centerX, centerY, radius)
                MascotMood.CELEBRATING -> drawCelebrationSparkles(centerX, centerY, radius)
            }
        }
    }
}

private fun DrawScope.drawMascotFace(
    mood: MascotMood,
    centerX: Float,
    centerY: Float,
    radius: Float
) {
    val eyeSpacing = radius * 0.38f
    val eyeY = centerY - radius * 0.05f
    val eyeRadius = radius * 0.12f

    when (mood) {
        MascotMood.WAVING -> {
            // Big Friendly Round Eyes
            drawCircle(Color.White, radius = eyeRadius, center = Offset(centerX - eyeSpacing, eyeY))
            drawCircle(Color.White, radius = eyeRadius, center = Offset(centerX + eyeSpacing, eyeY))
            // Pupils
            drawCircle(Color(0xFF0F172A), radius = eyeRadius * 0.65f, center = Offset(centerX - eyeSpacing, eyeY))
            drawCircle(Color(0xFF0F172A), radius = eyeRadius * 0.65f, center = Offset(centerX + eyeSpacing, eyeY))
            // Eye Sparkle
            drawCircle(Color.White, radius = eyeRadius * 0.25f, center = Offset(centerX - eyeSpacing - 2f, eyeY - 2f))
            drawCircle(Color.White, radius = eyeRadius * 0.25f, center = Offset(centerX + eyeSpacing - 2f, eyeY - 2f))

            // Rosy Cheeks
            drawCircle(Color(0x55F472B6), radius = eyeRadius * 0.7f, center = Offset(centerX - eyeSpacing - 10f, eyeY + 12f))
            drawCircle(Color(0x55F472B6), radius = eyeRadius * 0.7f, center = Offset(centerX + eyeSpacing + 10f, eyeY + 12f))

            // Cheerful Open Smile
            val smilePath = Path().apply {
                moveTo(centerX - 14f, eyeY + 16f)
                quadraticBezierTo(centerX, eyeY + 28f, centerX + 14f, eyeY + 16f)
            }
            drawPath(smilePath, color = Color.White, style = Stroke(width = 4f))
        }

        MascotMood.EXPLAINING -> {
            // Inquisitive Open Eyes
            drawCircle(Color.White, radius = eyeRadius * 1.1f, center = Offset(centerX - eyeSpacing, eyeY))
            drawCircle(Color.White, radius = eyeRadius * 1.1f, center = Offset(centerX + eyeSpacing, eyeY))
            // Pupils looking slightly up-right
            drawCircle(Color(0xFF0F172A), radius = eyeRadius * 0.6f, center = Offset(centerX - eyeSpacing + 2f, eyeY - 2f))
            drawCircle(Color(0xFF0F172A), radius = eyeRadius * 0.6f, center = Offset(centerX + eyeSpacing + 2f, eyeY - 2f))

            // Curious Small 'O' Mouth
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = Offset(centerX, eyeY + 18f),
                style = Stroke(width = 3.5f)
            )
        }

        MascotMood.CELEBRATING -> {
            // Joyful Happy Arc Eyes (^ ^)
            val leftEyeArc = Path().apply {
                moveTo(centerX - eyeSpacing - 12f, eyeY + 4f)
                quadraticBezierTo(centerX - eyeSpacing, eyeY - 10f, centerX - eyeSpacing + 12f, eyeY + 4f)
            }
            val rightEyeArc = Path().apply {
                moveTo(centerX + eyeSpacing - 12f, eyeY + 4f)
                quadraticBezierTo(centerX + eyeSpacing, eyeY - 10f, centerX + eyeSpacing + 12f, eyeY + 4f)
            }
            drawPath(leftEyeArc, color = Color.White, style = Stroke(width = 5f))
            drawPath(rightEyeArc, color = Color.White, style = Stroke(width = 5f))

            // Rosy Cheeks
            drawCircle(Color(0x77F472B6), radius = eyeRadius * 0.8f, center = Offset(centerX - eyeSpacing - 10f, eyeY + 12f))
            drawCircle(Color(0x77F472B6), radius = eyeRadius * 0.8f, center = Offset(centerX + eyeSpacing + 10f, eyeY + 12f))

            // Big Celebratory Open Mouth
            val openMouthPath = Path().apply {
                moveTo(centerX - 18f, eyeY + 14f)
                quadraticBezierTo(centerX, eyeY + 34f, centerX + 18f, eyeY + 14f)
                close()
            }
            drawPath(openMouthPath, color = Color(0xFFE11D48), style = Fill)
            drawPath(openMouthPath, color = Color.White, style = Stroke(width = 3f))
        }
    }
}

private fun DrawScope.drawWavingHand(centerX: Float, centerY: Float, radius: Float) {
    // Cute waving hand sprout on upper right
    val handX = centerX + radius * 0.82f
    val handY = centerY - radius * 0.35f
    drawCircle(
        color = Color(0xFF38BDF8),
        radius = radius * 0.22f,
        center = Offset(handX, handY)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.6f),
        radius = radius * 0.08f,
        center = Offset(handX - 2f, handY - 2f)
    )
}

private fun DrawScope.drawPointingBadge(centerX: Float, centerY: Float, radius: Float) {
    // Protective Shield Icon floating on bottom right
    val shieldX = centerX + radius * 0.68f
    val shieldY = centerY + radius * 0.45f
    val shieldRadius = radius * 0.24f

    drawCircle(
        color = Color(0xFF0F172A),
        radius = shieldRadius,
        center = Offset(shieldX, shieldY)
    )
    drawCircle(
        color = Color(0xFF38BDF8),
        radius = shieldRadius,
        center = Offset(shieldX, shieldY),
        style = Stroke(width = 3f)
    )
}

private fun DrawScope.drawCelebrationSparkles(centerX: Float, centerY: Float, radius: Float) {
    val sparkleColor = Color(0xFFFBBF24) // Golden Amber
    val sparkles = listOf(
        Offset(centerX - radius * 0.85f, centerY - radius * 0.65f),
        Offset(centerX + radius * 0.85f, centerY - radius * 0.65f),
        Offset(centerX - radius * 0.75f, centerY + radius * 0.55f),
        Offset(centerX + radius * 0.80f, centerY + radius * 0.50f)
    )

    for (pos in sparkles) {
        val s = 10f
        val path = Path().apply {
            moveTo(pos.x, pos.y - s)
            lineTo(pos.x + s * 0.3f, pos.y - s * 0.3f)
            lineTo(pos.x + s, pos.y)
            lineTo(pos.x + s * 0.3f, pos.y + s * 0.3f)
            lineTo(pos.x, pos.y + s)
            lineTo(pos.x - s * 0.3f, pos.y + s * 0.3f)
            lineTo(pos.x - s, pos.y)
            lineTo(pos.x - s * 0.3f, pos.y - s * 0.3f)
            close()
        }
        drawPath(path, color = sparkleColor, style = Fill)
    }
}
