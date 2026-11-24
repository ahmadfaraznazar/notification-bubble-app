package com.bubble.ui.organisms

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import com.bubble.ui.interaction.BubblePhysicsController
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests validating magnetic dismiss zone detection and release routing.
 */
class DismissTargetZoneTest {

    private val density = Density(density = 2.0f)
    private val screenWidth = 1080f
    private val screenHeight = 2400f
    private val statusBar = 80f
    private val navBar = 120f

    private lateinit var controller: BubblePhysicsController

    @Before
    fun setUp() {
        controller = BubblePhysicsController(
            density = density,
            screenWidthPx = screenWidth,
            screenHeightPx = screenHeight,
            statusBarHeightPx = statusBar,
            navBarHeightPx = navBar
        )
    }

    @Test
    fun dismissTargetCenter_isPositionedAtBottomCenter() {
        val targetCenter = controller.dismissTargetCenter
        assertEquals(screenWidth / 2f, targetCenter.x, 0.01f)
        // 2400 - 120 - 48dp (96px) = 2184px
        assertEquals(2184f, targetCenter.y, 0.01f)
    }

    @Test
    fun handleRelease_whenInsideMagneticZone_executesDismissCallback() = runTest {
        var wasDismissed = false

        // Place bubble inside magnetic zone
        controller.offset.snapTo(controller.dismissSnapOffset)
        assertTrue(controller.checkMagneticZone(controller.offset.value))

        controller.handleRelease(currentVelocityX = 0f) {
            wasDismissed = true
        }

        assertTrue(wasDismissed)
    }

    @Test
    fun handleRelease_whenOutsideMagneticZone_snapsToEdgeAndDoesNotDismiss() = runTest {
        var wasDismissed = false

        // Place bubble in top-left resting zone
        controller.offset.snapTo(Offset(controller.minX, 300f))
        assertFalse(controller.checkMagneticZone(controller.offset.value))

        controller.handleRelease(currentVelocityX = 0f) {
            wasDismissed = true
        }

        assertFalse(wasDismissed)
        assertEquals(controller.minX, controller.offset.value.x, 0.01f)
    }
}
