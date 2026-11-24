package com.bubble.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bubble.ui.theme.BubbleDimensions
import com.bubble.ui.theme.BubbleTheme

/**
 * High-contrast Unread Counter Pill Badge visual primitive.
 * Enforces tabular digits ("tnum") and overflow formatting ("99+").
 */
@Composable
fun UnreadCounterBadge(
    unreadCount: Int,
    modifier: Modifier = Modifier
) {
    if (unreadCount <= 0) return

    val displayString = if (unreadCount > 99) "99+" else unreadCount.toString()

    Box(
        modifier = modifier
            .height(BubbleDimensions.UnreadBadgeHeight)
            .defaultMinSize(minWidth = BubbleDimensions.UnreadBadgeMinWidth)
            .clip(CircleShape)
            .background(BubbleTheme.colors.unreadBadgeBg)
            .padding(horizontal = BubbleDimensions.UnreadBadgePaddingHorizontal)
            .semantics {
                contentDescription = "$unreadCount unread notifications"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayString,
            color = BubbleTheme.colors.unreadBadgeText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontFeatureSettings = "tnum")
        )
    }
}
