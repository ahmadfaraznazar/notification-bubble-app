package com.bubble.ui.organisms

import com.bubble.domain.model.NotificationMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating preview card state management and tap-to-unmask privacy rules.
 */
class NotificationPreviewCardTest {

    private val standardMessage = NotificationMessage(
        id = "id_1",
        senderName = "Alice",
        textSnippet = "Hey, are you free for lunch?",
        packageName = "com.whatsapp",
        appName = "WhatsApp",
        isMasked = false
    )

    private val sensitiveMessage = NotificationMessage(
        id = "id_2",
        senderName = "Chase Bank",
        textSnippet = "Your OTP is 482910. Do not share.",
        packageName = "com.chase",
        appName = "Chase",
        isMasked = true
    )

    @Test
    fun displaySnippet_whenNotMasked_showsOriginalSnippet() {
        val state = PreviewCardUiState(
            notification = standardMessage,
            isVisible = true,
            isContentUnmasked = false
        )
        assertEquals("Hey, are you free for lunch?", state.displaySnippet)
    }

    @Test
    fun displaySnippet_whenMaskedAndNotUnmasked_showsMaskedDots() {
        val state = PreviewCardUiState(
            notification = sensitiveMessage,
            isVisible = true,
            isContentUnmasked = false,
            unmaskedOverrideText = sensitiveMessage.textSnippet
        )
        assertEquals("•••••• (Tap to view)", state.displaySnippet)
    }

    @Test
    fun displaySnippet_whenMaskedAndUnmasked_showsOriginalSecretText() {
        val state = PreviewCardUiState(
            notification = sensitiveMessage,
            isVisible = true,
            isContentUnmasked = true,
            unmaskedOverrideText = sensitiveMessage.textSnippet
        )
        assertEquals("Your OTP is 482910. Do not share.", state.displaySnippet)
    }

    @Test
    fun viewModel_toggleUnmask_flipsUnmaskStateForMaskedNotificationsOnly() {
        val viewModel = PreviewCardViewModel()
        viewModel.showNotification(sensitiveMessage)

        assertFalse(viewModel.uiState.value.isContentUnmasked)
        assertEquals("•••••• (Tap to view)", viewModel.uiState.value.displaySnippet)

        viewModel.toggleUnmask()
        assertTrue(viewModel.uiState.value.isContentUnmasked)
        assertEquals("Your OTP is 482910. Do not share.", viewModel.uiState.value.displaySnippet)

        viewModel.toggleUnmask()
        assertFalse(viewModel.uiState.value.isContentUnmasked)
    }

    @Test
    fun viewModel_hide_setsVisibilityToFalse() {
        val viewModel = PreviewCardViewModel()
        viewModel.showNotification(standardMessage)
        assertTrue(viewModel.uiState.value.isVisible)

        viewModel.hide()
        assertFalse(viewModel.uiState.value.isVisible)
    }
}
