package com.bubble.ui.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating step navigation and permission gating rules for the Mascot Onboarding Wizard.
 */
class MascotWizardLogicTest {

    data class WizardState(
        val currentStep: Int = 0,
        val hasOverlayPermission: Boolean = false,
        val hasNotificationPermission: Boolean = false
    ) {
        val totalSteps: Int = 4

        val canAdvance: Boolean
            get() = when (currentStep) {
                0 -> true
                1 -> hasOverlayPermission
                2 -> hasNotificationPermission
                3 -> true
                else -> false
            }

        val isComplete: Boolean
            get() = currentStep >= 3

        fun nextStep(): WizardState {
            return if (canAdvance && currentStep < totalSteps - 1) {
                copy(currentStep = currentStep + 1)
            } else {
                this
            }
        }

        fun prevStep(): WizardState {
            return if (currentStep > 0) {
                copy(currentStep = currentStep - 1)
            } else {
                this
            }
        }
    }

    @Test
    fun initialState_startsAtWelcomeStep() {
        val state = WizardState()
        assertEquals(0, state.currentStep)
        assertFalse(state.isComplete)
        assertTrue(state.canAdvance)
    }

    @Test
    fun fromWelcome_canAlwaysAdvanceToOverlayStep() {
        val state = WizardState().nextStep()
        assertEquals(1, state.currentStep)
    }

    @Test
    fun overlayStep_cannotAdvanceWithoutPermission() {
        val state = WizardState(currentStep = 1, hasOverlayPermission = false)
        assertFalse(state.canAdvance)
        assertEquals(1, state.nextStep().currentStep)
    }

    @Test
    fun overlayStep_advancesWhenPermissionGranted() {
        val state = WizardState(currentStep = 1, hasOverlayPermission = true).nextStep()
        assertEquals(2, state.currentStep)
    }

    @Test
    fun notificationStep_cannotAdvanceWithoutPermission() {
        val state = WizardState(currentStep = 2, hasNotificationPermission = false)
        assertFalse(state.canAdvance)
        assertEquals(2, state.nextStep().currentStep)
    }

    @Test
    fun notificationStep_advancesWhenPermissionGranted() {
        val state = WizardState(currentStep = 2, hasNotificationPermission = true).nextStep()
        assertEquals(3, state.currentStep)
        assertTrue(state.isComplete)
    }

    @Test
    fun backNavigation_decrementsStepUntilWelcome() {
        val state = WizardState(currentStep = 2)
        val step1 = state.prevStep()
        assertEquals(1, step1.currentStep)
        val step0 = step1.prevStep()
        assertEquals(0, step0.currentStep)
        val clamped = step0.prevStep()
        assertEquals(0, clamped.currentStep)
    }
}
