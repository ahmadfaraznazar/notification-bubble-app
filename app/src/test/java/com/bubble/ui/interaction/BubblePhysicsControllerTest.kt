package com.bubble.ui.interaction

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying zero-allocation physics math, edge clamping, and snap targets.
 */
class BubblePhysicsControllerTest {

    private val density = Density(density = 2.0f) // 1dp = 2px
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
    fun whenInitialized_boundsEnforce16dpSafeMargins() {
        // 16dp * 2 = 32px
        assertEquals(32f, controller.minX, 0.01f)
        // 56dp * 2 = 112px; maxX = 1080 - 32 - 112 = 936px
        assertEquals(936f, controller.maxX, 0.01f)
        // minY = 80 + 32 = 112px
        assertEquals(112f, controller.minY, 0.01f)
        // maxY = 2400 - 120 - 112 - 32 = 2136px
        assertEquals(2136f, controller.maxY, 0.01f)
    }

    @Test
    fun whenReleasedOnLeftHalfWithoutVelocity_snapsToLeftEdge() = runTest {
        // Set position on left half (e.g. 200px)
        controller.offset.snapTo(Offset(200f, 500f))
        val targetX = controller.calculateTargetX(currentVelocityX = 0f)
        assertEquals(controller.minX, targetX, 0.01f)
    }

    @Test
    fun whenReleasedOnRightHalfWithoutVelocity_snapsToRightEdge() = runTest {
        // Set position on right half (e.g. 800px)
        controller.offset.snapTo(Offset(800f, 500f))
        val targetX = controller.calculateTargetX(currentVelocityX = 0f)
        assertEquals(controller.maxX, targetX, 0.01f)
    }

    @Test
    fun whenFlingVelocityPositive_snapsToRightEdgeEvenIfOnLeftHalf() = runTest {
        controller.offset.snapTo(Offset(200f, 500f))
        val targetX = controller.calculateTargetX(currentVelocityX = 1200f)
        assertEquals(controller.maxX, targetX, 0.01f)
    }

    @Test
    fun whenFlingVelocityNegative_snapsToLeftEdgeEvenIfOnRightHalf() = runTest {
        controller.offset.snapTo(Offset(800f, 500f))
        val targetX = controller.calculateTargetX(currentVelocityX = -1200f)
        assertEquals(controller.minX, targetX, 0.01f)
    }

    @Test
    fun whenNearDismissTarget_magneticZoneReturnsTrue() {
        // Dismiss target is at screenWidth / 2 = 540f
        // Position bubble center close to dismissTarget
        val targetCenter = controller.dismissTargetCenter
        val bubbleOffset = Offset(
            x = targetCenter.x - controller.bubbleDiameterPx / 2f,
            y = targetCenter.y - controller.bubbleDiameterPx / 2f
        )
        assertTrue(controller.checkMagneticZone(bubbleOffset))
    }

    @Test
    fun whenFarFromDismissTarget_magneticZoneReturnsFalse() {
        val bubbleOffset = Offset(controller.minX, controller.minY)
        assertFalse(controller.checkMagneticZone(bubbleOffset))
    }
}
