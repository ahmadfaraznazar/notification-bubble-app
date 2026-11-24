package com.bubble.service.action

import android.app.Notification
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bubble.core.logging.BubbleLog
import java.util.concurrent.ConcurrentHashMap

/**
 * Dispatches inline quick replies directly to Android OS via Notification.Action
 * and RemoteInput without requiring network permissions or intermediate server hops.
 */
object NotificationActionDispatcher {

    private const val TAG = "ActionDispatcher"

    // Thread-safe map of pending quick-reply actions keyed by notificationId
    private val pendingActions = ConcurrentHashMap<String, Notification.Action>()

    /**
     * Registers an intercepted notification action capable of remote input.
     */
    fun registerAction(notificationId: String, action: Notification.Action) {
        pendingActions[notificationId] = action
    }

    /**
     * Removes an action upon notification dismissal.
     */
    fun removeAction(notificationId: String) {
        pendingActions.remove(notificationId)
    }

    /**
     * Clears all pending actions.
     */
    fun clearAll() {
        pendingActions.clear()
    }

    /**
     * Checks if a notification has an available quick-reply action.
     */
    fun hasReplyAction(notificationId: String): Boolean {
        return pendingActions.containsKey(notificationId)
    }

    /**
     * Returns total pending action count.
     */
    fun pendingCount(): Int = pendingActions.size

    /**
     * Prepares the results bundle for the RemoteInput key.
     */
    fun createReplyBundle(resultKey: String, replyText: String): Bundle {
        return Bundle().apply {
            putCharSequence(resultKey, replyText)
        }
    }

    /**
     * Dispatches the user's reply directly to the originating application's PendingIntent.
     */
    fun dispatchReply(context: Context, notificationId: String, replyText: String): Boolean {
        val action = pendingActions[notificationId]
        if (action == null) {
            BubbleLog.w(TAG, "Cannot dispatch reply: No action registered for id=$notificationId")
            return false
        }

        val remoteInputs = action.remoteInputs
        if (remoteInputs.isNullOrEmpty()) {
            BubbleLog.w(TAG, "Cannot dispatch reply: Action has no RemoteInputs")
            return false
        }

        val remoteInput = remoteInputs.first()
        val replyIntent = Intent()
        val bundle = createReplyBundle(remoteInput.resultKey, replyText)
        RemoteInput.addResultsToIntent(arrayOf(remoteInput), replyIntent, bundle)

        return try {
            action.actionIntent.send(context, 0, replyIntent)
            BubbleLog.i(TAG, "Quick reply dispatched successfully for id=$notificationId (length=${replyText.length})")
            pendingActions.remove(notificationId)
            true
        } catch (e: Exception) {
            BubbleLog.e(TAG, "Failed to send quick reply PendingIntent for id=$notificationId", e)
            false
        }
    }
}
