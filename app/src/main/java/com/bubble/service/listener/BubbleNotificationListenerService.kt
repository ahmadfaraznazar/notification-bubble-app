package com.bubble.service.listener

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.bubble.core.logging.BubbleLog
import com.bubble.data.repository.NotificationRepositoryImpl
import com.bubble.domain.model.NotificationMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * System NotificationListenerService intercepting incoming alerts from third-party apps.
 * Filters ongoing / foreground / silent alerts and feeds clean models to NotificationRepository.
 */
class BubbleNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val repository = NotificationRepositoryImpl.getInstance()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val notification = sbn.notification ?: return
        val packageName = sbn.packageName ?: return

        // 1. Ignore self-emitted notifications (e.g., our own foreground service status)
        if (packageName == this.packageName) {
            return
        }

        // 2. Ignore ongoing and foreground service notifications (e.g., media players, downloads)
        val flags = notification.flags
        val isOngoing = (flags and Notification.FLAG_ONGOING_EVENT) != 0 ||
            (flags and Notification.FLAG_FOREGROUND_SERVICE) != 0
        if (isOngoing) {
            return
        }

        // 3. Extract sender, message body, and metadata
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

        // 4. Resolve friendly app name
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        // 5. Detect inline quick-reply action support
        val hasQuickReply = notification.actions?.any { action ->
            action.remoteInputs?.isNotEmpty() == true
        } ?: false

        // 6. Assemble domain model with unique composite ID
        val notificationId = "${packageName}_${sbn.id}_${sbn.tag.orEmpty()}"
        val message = NotificationMessage(
            id = notificationId,
            senderName = sender,
            textSnippet = body,
            packageName = packageName,
            appName = appName,
            avatarUri = null,
            timestampMs = sbn.postTime,
            isMasked = false,
            hasQuickReply = hasQuickReply
        )

        // 7. Privacy-safe redacted logging
        BubbleLog.logNotificationReceived(
            packageName = packageName,
            bodyLength = body.length,
            isMasked = false
        )

        // 8. Enqueue into reactive repository
        serviceScope.launch {
            repository.enqueueNotification(message)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        if (packageName == this.packageName) return

        val notificationId = "${packageName}_${sbn.id}_${sbn.tag.orEmpty()}"
        BubbleLog.d("NotificationListener", "Dismissing notification: id=$notificationId")

        serviceScope.launch {
            repository.dismissNotification(notificationId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
