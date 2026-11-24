package com.bubble.ui.onboarding

import com.bubble.domain.model.AppRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating App Picker search filtering, counts, and Chats Only preset rules.
 */
class AppPickerLogicTest {

    private val sampleApps = listOf(
        AppRule("com.whatsapp", "WhatsApp", isWhitelisted = false),
        AppRule("org.telegram.messenger", "Telegram", isWhitelisted = true),
        AppRule("com.Slack", "Slack", isWhitelisted = false),
        AppRule("com.spotify.music", "Spotify", isWhitelisted = false),
        AppRule("com.google.android.apps.messaging", "Messages", isWhitelisted = true)
    )

    @Test
    fun filteredApps_withBlankQuery_returnsAllApps() {
        val state = AppPickerUiState(apps = sampleApps, searchQuery = "")
        assertEquals(5, state.filteredApps.size)
    }

    @Test
    fun filteredApps_matchesAppNameCaseInsensitive() {
        val state = AppPickerUiState(apps = sampleApps, searchQuery = "tele")
        assertEquals(1, state.filteredApps.size)
        assertEquals("Telegram", state.filteredApps[0].appName)
    }

    @Test
    fun filteredApps_matchesPackageName() {
        val state = AppPickerUiState(apps = sampleApps, searchQuery = "spotify")
        assertEquals(1, state.filteredApps.size)
        assertEquals("Spotify", state.filteredApps[0].appName)
    }

    @Test
    fun enabledCount_countsOnlyWhitelistedApps() {
        val state = AppPickerUiState(apps = sampleApps)
        assertEquals(2, state.enabledCount) // Telegram and Messages
    }

    @Test
    fun chatsOnlyPreset_identifiesChatPackages() {
        assertTrue(AppPickerViewModel.CHAT_PRESET_PACKAGES.contains("com.whatsapp"))
        assertTrue(AppPickerViewModel.CHAT_PRESET_PACKAGES.contains("org.telegram.messenger"))
        assertTrue(AppPickerViewModel.CHAT_PRESET_PACKAGES.contains("org.thoughtcrime.securesms"))
        assertTrue(AppPickerViewModel.CHAT_PRESET_PACKAGES.contains("com.discord"))
        assertTrue(AppPickerViewModel.CHAT_PRESET_PACKAGES.contains("com.Slack"))
    }
}
