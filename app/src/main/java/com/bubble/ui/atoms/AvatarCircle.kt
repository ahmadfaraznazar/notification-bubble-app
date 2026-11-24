package com.bubble.ui.atoms

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bubble.ui.theme.BubbleDimensions
import com.bubble.ui.theme.BubbleTheme
import kotlin.math.abs

/**
 * 56dp Circular Avatar visual primitive.
 * Supports image loading via Coil with deterministic initials fallback.
 */
@Composable
fun AvatarCircle(
    senderName: String,
    avatarUri: Uri? = null,
    diameter: Dp = BubbleDimensions.BubbleSizeDefault,
    modifier: Modifier = Modifier
) {
    val initials = remember(senderName) {
        extractInitials(senderName)
    }

    val fallbackGradient = remember(senderName) {
        generateDeterministicGradient(senderName)
    }

    Box(
        modifier = modifier
            .size(diameter)
            .clip(CircleShape)
            .border(
                width = 1.5.dp,
                color = BubbleTheme.colors.borderStroke,
                shape = CircleShape
            )
            .semantics {
                contentDescription = "$senderName profile photo"
            },
        contentAlignment = Alignment.Center
    ) {
        if (avatarUri != null && avatarUri.toString().isNotBlank()) {
            AsyncImage(
                model = avatarUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            // Deterministic initials avatar fallback
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(fallbackGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (diameter.value * 0.35f).sp
                )
            }
        }
    }
}

/**
 * Extracts 1-2 uppercase characters from sender name.
 */
private fun extractInitials(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "?"

    val parts = trimmed.split("\\s+".toRegex())
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        trimmed.length >= 2 -> trimmed.take(2).uppercase()
        else -> trimmed.first().uppercase()
    }
}

/**
 * Generates an accessible, aesthetic two-tone gradient based on string hash code.
 */
private fun generateDeterministicGradient(key: String): Brush {
    val palette = listOf(
        Pair(Color(0xFF2563EB), Color(0xFF1D4ED8)), // Blue
        Pair(Color(0xFF7C3AED), Color(0xFF6D28D9)), // Violet
        Pair(Color(0xFFDB2777), Color(0xFFBE185D)), // Pink
        Pair(Color(0xFF059669), Color(0xFF047857)), // Emerald
        Pair(Color(0xFFD97706), Color(0xFFB45309)), // Amber
        Pair(Color(0xFF0891B2), Color(0xFF0E7490))  // Cyan
    )
    val index = abs(key.hashCode()) % palette.size
    val (start, end) = palette[index]
    return Brush.linearGradient(listOf(start, end))
}
