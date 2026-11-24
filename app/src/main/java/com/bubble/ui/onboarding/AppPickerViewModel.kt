package com.bubble.ui.onboarding

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubble.data.local.AppDatabase
import com.bubble.data.repository.RuleRepositoryImpl
import com.bubble.domain.model.AppRule
import com.bubble.domain.repository.RuleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI state for the App Picker configuration screen.
 */
data class AppPickerUiState(
    val apps: List<AppRule> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
) {
    val filteredApps: List<AppRule>
        get() = if (searchQuery.isBlank()) {
            apps
        } else {
            apps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }

    val enabledCount: Int
        get() = apps.count { it.isWhitelisted }
}

/**
 * ViewModel managing installed app enumeration, search filtering, and Room SQLite persistence.
 */
class AppPickerViewModel(
    private val ruleRepository: RuleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppPickerUiState())
    val uiState: StateFlow<AppPickerUiState> = _uiState.asStateFlow()

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val existingRules = ruleRepository.observeAllRules().first().associateBy { it.packageName }

            val scannedApps = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, 0)

                resolveInfos.mapNotNull { resolveInfo ->
                    val pkg = resolveInfo.activityInfo.packageName
                    // Ignore own package
                    if (pkg == context.packageName) return@mapNotNull null

                    val label = resolveInfo.loadLabel(pm).toString()
                    val existing = existingRules[pkg]

                    existing ?: AppRule(
                        packageName = pkg,
                        appName = label,
                        isWhitelisted = false, // 0 Default Law
                        isCustomRule = false
                    )
                }.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }
            }

            _uiState.value = _uiState.value.copy(
                apps = scannedApps,
                isLoading = false
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onToggleApp(packageName: String, isEnabled: Boolean) {
        val updatedList = _uiState.value.apps.map { app ->
            if (app.packageName == packageName) {
                app.copy(isWhitelisted = isEnabled, isCustomRule = true)
            } else {
                app
            }
        }
        _uiState.value = _uiState.value.copy(apps = updatedList)

        viewModelScope.launch {
            ruleRepository.setAppWhitelistStatus(packageName, isEnabled)
        }
    }

    /**
     * 1-Tap Preset: Activates common messaging & chat apps.
     */
    fun selectChatsOnlyPreset() {
        val updatedList = _uiState.value.apps.map { app ->
            val isChatApp = CHAT_PRESET_PACKAGES.contains(app.packageName)
            app.copy(isWhitelisted = isChatApp)
        }
        _uiState.value = _uiState.value.copy(apps = updatedList)

        viewModelScope.launch {
            updatedList.forEach { app ->
                ruleRepository.setAppWhitelistStatus(app.packageName, app.isWhitelisted)
            }
        }
    }

    fun selectAll() {
        val updatedList = _uiState.value.apps.map { it.copy(isWhitelisted = true) }
        _uiState.value = _uiState.value.copy(apps = updatedList)

        viewModelScope.launch {
            updatedList.forEach { app ->
                ruleRepository.setAppWhitelistStatus(app.packageName, true)
            }
        }
    }

    fun clearAll() {
        val updatedList = _uiState.value.apps.map { it.copy(isWhitelisted = false) }
        _uiState.value = _uiState.value.copy(apps = updatedList)

        viewModelScope.launch {
            updatedList.forEach { app ->
                ruleRepository.setAppWhitelistStatus(app.packageName, false)
            }
        }
    }

    companion object {
        val CHAT_PRESET_PACKAGES = setOf(
            "com.whatsapp",
            "org.telegram.messenger",
            "org.thoughtcrime.securesms",
            "com.facebook.orca",
            "com.google.android.apps.messaging",
            "com.discord",
            "com.Slack",
            "com.viber.voip"
        )

        fun create(context: Context): AppPickerViewModel {
            val db = AppDatabase.getInstance(context)
            return AppPickerViewModel(RuleRepositoryImpl(db.appRuleDao()))
        }
    }
}
