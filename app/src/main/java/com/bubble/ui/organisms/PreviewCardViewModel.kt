package com.bubble.ui.organisms

import androidx.lifecycle.ViewModel
import com.bubble.domain.model.NotificationMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI State for the floating Notification Preview Card.
 */
data class PreviewCardUiState(
    val notification: NotificationMessage? = null,
    val isVisible: Boolean = false,
    val isContentUnmasked: Boolean = false,
    val unmaskedOverrideText: String? = null
) {
    val displaySnippet: String
        get() = when {
            notification == null -> ""
            !notification.isMasked -> notification.textSnippet
            isContentUnmasked -> unmaskedOverrideText ?: notification.textSnippet
            else -> "•••••• (Tap to view)"
        }
}

/**
 * ViewModel managing the preview card state, tap-to-unmask privacy toggling,
 * and user interactions (Reply, Mark as Read, Dismiss).
 */
class PreviewCardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PreviewCardUiState())
    val uiState: StateFlow<PreviewCardUiState> = _uiState.asStateFlow()

    fun showNotification(notification: NotificationMessage, originalTextSnippet: String? = null) {
        _uiState.value = PreviewCardUiState(
            notification = notification,
            isVisible = true,
            isContentUnmasked = !notification.isMasked,
            unmaskedOverrideText = originalTextSnippet ?: notification.textSnippet
        )
    }

    fun hide() {
        _uiState.value = _uiState.value.copy(isVisible = false)
    }

    fun toggleUnmask() {
        val current = _uiState.value
        if (current.notification?.isMasked == true) {
            _uiState.value = current.copy(isContentUnmasked = !current.isContentUnmasked)
        }
    }
}
