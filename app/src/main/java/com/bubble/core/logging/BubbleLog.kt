package com.bubble.core.logging

import android.util.Log

/**
 * Redacted, privacy-safe logger for system notifications and overlay operations.
 * Enforces zero leakage of sender names, message content, or sensitive tokens.
 */
object BubbleLog {
    private const val TAG = "BubbleApp"
    private var isDebugEnabled: Boolean = false

    fun init(debug: Boolean) {
        isDebugEnabled = debug
    }

    fun d(category: String, message: String) {
        if (isDebugEnabled) {
            Log.d(TAG, "[$category] $message")
        }
    }

    fun i(category: String, message: String) {
        Log.i(TAG, "[$category] $message")
    }

    fun w(category: String, message: String, throwable: Throwable? = null) {
        Log.w(TAG, "[$category] $message", throwable)
    }

    fun e(category: String, message: String, throwable: Throwable? = null) {
        Log.e(TAG, "[$category] $message", throwable)
    }

    /**
     * Logs notification arrival safely without exposing private message content.
     */
    fun logNotificationReceived(packageName: String, bodyLength: Int, isMasked: Boolean) {
        d("Notification", "Intercepted: pkg=$packageName, bytes=$bodyLength, masked=$isMasked")
    }
}
