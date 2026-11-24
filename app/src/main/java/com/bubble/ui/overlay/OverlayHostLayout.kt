package com.bubble.ui.overlay

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import java.util.concurrent.ConcurrentHashMap

/**
 * Custom FrameLayout overlay host responsible for transparent touch pass-through.
 *
 * When touch coordinates fall outside registered interactive bounds (the bubble
 * or flyout cards), dispatchTouchEvent returns false, allowing the OS to pass
 * gestures straight through to underlying applications with zero delay.
 */
class OverlayHostLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Thread-safe map of active interactive bounds (e.g., "bubble_0", "preview_card")
    private val interactiveBounds = ConcurrentHashMap<String, Rect>()

    // Tracks whether the active gesture started inside an interactive target
    private var isTrackingActiveTouch: Boolean = false

    /**
     * Registers or updates an interactive screen region.
     */
    fun updateTargetBounds(tag: String, bounds: Rect) {
        interactiveBounds[tag] = bounds
    }

    /**
     * Unregisters an interactive region.
     */
    fun removeTargetBounds(tag: String) {
        interactiveBounds.remove(tag)
    }

    /**
     * Clears all interactive touch regions.
     */
    fun clearTargetBounds() {
        interactiveBounds.clear()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Check if the touch falls inside any registered interactive element
                isTrackingActiveTouch = interactiveBounds.values.any { rect ->
                    rect.contains(x, y)
                }

                if (!isTrackingActiveTouch) {
                    // Touch is outside all interactive components; pass through to underlying window
                    return false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val wasTracking = isTrackingActiveTouch
                isTrackingActiveTouch = false
                if (!wasTracking) {
                    return false
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isTrackingActiveTouch) {
                    return false
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onApplyWindowInsets(insets: android.view.WindowInsets): android.view.WindowInsets {
        for (i in 0 until childCount) {
            getChildAt(i).dispatchApplyWindowInsets(insets)
        }
        return super.onApplyWindowInsets(insets)
    }
}
