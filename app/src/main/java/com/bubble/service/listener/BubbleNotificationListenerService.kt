package com.bubble.service.listener

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.bubble.core.logging.BubbleLog
import com.bubble.data.local.AppDatabase
import com.bubble.data.repository.NotificationRepositoryImpl
import com.bubble.data.repository.RuleRepositoryImpl
import com.bubble.domain.model.AppRule
import com.bubble.domain.model.NotificationMessage
import com.bubble.domain.util.SensitiveContentFilter
import com.bubble.service.action.NotificationActionDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * System NotificationListenerService intercepting incoming alerts from third-party apps.
 * Enforces Room-backed package whitelisting, quiet hours filtering, and automated 2FA/OTP masking.
 */
class BubbleNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val notificationRepository = NotificationRepositoryImpl.getInstance()
    private lateinit var ruleRepository: RuleRepositoryImpl

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(applicationContext)
        ruleRepository = RuleRepositoryImpl(database.appRuleDao())

        // Seed default messaging apps into Room whitelist
        serviceScope.launch {
            ruleRepository.initializeDefaultRules(DEFAULT_PRESET_RULES)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val notification = sbn.notification ?: return
        val packageName = sbn.packageName ?: return

        // 1. Ignore self-emitted notifications (e.g. our foreground service status)
        if (packageName == this.packageName) {
            return
        }

        // 2. Security Drop: Never spawn bubbles for authenticator apps (Google Authenticator, Authy, etc.)
        if (SensitiveContentFilter.isBlacklistedPackage(packageName)) {
            BubbleLog.d("NotificationListener", "Security drop: Package $packageName is an authenticator app")
            return
        }

        // 3. Ignore ongoing and foreground service notifications
        val flags = notification.flags
        val isOngoing = (flags and Notification.FLAG_ONGOING_EVENT) != 0 ||
            (flags and Notification.FLAG_FOREGROUND_SERVICE) != 0
        if (isOngoing) {
            return
        }

        // 4. Extract sender, message body, and metadata
        val extras = notification.extras
        val sender = extras.getString(Notification.EXTRA_CONVERSATION_TITLE)
            ?: extras.getString(Notification.EXTRA_TITLE)
            ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: "Unknown"

        val body = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: ""

        // Discard empty/null notification alerts
        if (sender.isBlank() && body.isBlank()) {
            return
        }

        // 5. Resolve friendly app name
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        // 6. Evaluate Room database whitelist & quiet hours policy
        serviceScope.launch {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isAllowed = ruleRepository.isPackageAllowed(packageName, currentHour)

            if (!isAllowed) {
                BubbleLog.d("NotificationListener", "Dropped notification: pkg=$packageName not whitelisted or quiet hours active")
                return@launch
            }

            // 7. Heuristic Sensitive Content & OTP/2FA Detection
            val isSensitive = SensitiveContentFilter.isSensitive(packageName, sender, body)
            val displaySnippet = if (isSensitive) {
                SensitiveContentFilter.maskSnippet()
            } else {
                body
            }

            // 8. Detect inline quick-reply action support
            val replyAction = notification.actions?.firstOrNull { action ->
                action.remoteInputs?.isNotEmpty() == true
            }
            val hasQuickReply = replyAction != null

            // 9. Assemble domain model with unique composite ID
            val notificationId = "${packageName}_${sbn.id}_${sbn.tag.orEmpty()}"

            // Cache quick-reply action for zero-latency local dispatch
            if (replyAction != null) {
                NotificationActionDispatcher.registerAction(notificationId, replyAction)
            }

            val message = NotificationMessage(
                id = notificationId,
                senderName = sender,
                textSnippet = displaySnippet,
                packageName = packageName,
                appName = appName,
                avatarUri = null,
                timestampMs = sbn.postTime,
                isMasked = isSensitive,
                hasQuickReply = hasQuickReply
            )

            // 10. Privacy-safe redacted logging
            BubbleLog.logNotificationReceived(
                packageName = packageName,
                bodyLength = body.length,
                isMasked = isSensitive
            )

            // 11. Enqueue into reactive repository
            notificationRepository.enqueueNotification(message)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        if (packageName == this.packageName) return

        val notificationId = "${packageName}_${sbn.id}_${sbn.tag.orEmpty()}"
        BubbleLog.d("NotificationListener", "Dismissing notification: id=$notificationId")
        NotificationActionDispatcher.removeAction(notificationId)

        serviceScope.launch {
            notificationRepository.dismissNotification(notificationId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        val DEFAULT_PRESET_RULES = listOf(
            AppRule("org.telegram.messenger", "Telegram", isWhitelisted = true),
            AppRule("com.whatsapp", "WhatsApp", isWhitelisted = true),
            AppRule("com.google.android.apps.messaging", "Messages", isWhitelisted = true),
            AppRule("com.facebook.orca", "Messenger", isWhitelisted = true),
            AppRule("com.Slack", "Slack", isWhitelisted = true)
        )
    }
}
