package com.bubble.ui.atoms

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bubble.ui.theme.BubbleDimensions
import com.bubble.ui.theme.BubbleTheme

/**
 * 18dp App Source Badge visual primitive.
 * Anchored to the bottom-right corner of the floating avatar with a 2dp cutout stroke.
 */
@Composable
fun AppBadgeIcon(
    appName: String,
    @DrawableRes iconRes: Int? = null,
    size: Dp = BubbleDimensions.AppBadgeSize,
    cutoutPadding: Dp = BubbleDimensions.AppBadgeCutoutPadding,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size + cutoutPadding * 2)
            .border(
                width = cutoutPadding,
                color = BubbleTheme.colors.appBadgeBackground,
                shape = CircleShape
            )
            .padding(cutoutPadding)
            .clip(CircleShape)
            .background(Color(0xFF1E293B)) // Fallback dark container
            .semantics {
                contentDescription = "Delivered by $appName"
            },
        contentAlignment = Alignment.Center
    ) {
        if (iconRes != null && iconRes != 0) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.matchParentSize()
            )
        } else {
            // Minimal initial glyph fallback
            Text(
                text = appName.take(1).uppercase(),
                color = Color.White,
                fontSize = (size.value * 0.55f).sp
            )
        }
    }
}
