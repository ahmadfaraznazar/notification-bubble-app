package com.bubble.service.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.bubble.R
import com.bubble.core.logging.BubbleLog
import com.bubble.ui.MainActivity
import com.bubble.ui.overlay.BubbleOverlayContent
import com.bubble.ui.overlay.OverlayHostLayout

/**
 * Resilient foreground service hosting the full-screen transparent WindowManager overlay.
 * Configured with START_STICKY, low-priority ongoing notification, and lifecycle resilience.
 */
class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayHostLayout: OverlayHostLayout? = null
    private val lifecycleOwner = OverlayLifecycleOwner()
    private var isOverlayAttached: Boolean = false

    override fun onCreate() {
        super.onCreate()
        BubbleLog.i(TAG, "Initializing OverlayService and WindowManager host")

        lifecycleOwner.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        startForegroundNotification()
        attachOverlayWindow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        BubbleLog.i(TAG, "onStartCommand received action: $action")

        when (action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                lifecycleOwner.onStart()
                if (!isOverlayAttached) {
                    attachOverlayWindow()
                }
            }
        }

        // START_STICKY instructs OS to recreate service if terminated under low memory
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Attaches the ComposeView hierarchy to Android WindowManager.
     */
    private fun attachOverlayWindow() {
        if (isOverlayAttached) return

        if (!Settings.canDrawOverlays(this)) {
            BubbleLog.w(TAG, "Cannot attach overlay: SYSTEM_ALERT_WINDOW permission missing")
            return
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }

        val host = OverlayHostLayout(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService.lifecycleOwner)
            setViewTreeViewModelStoreOwner(this@OverlayService.lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(this@OverlayService.lifecycleOwner)
        }

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                BubbleOverlayContent(
                    onBoundsUpdated = { tag, rect ->
                        host.updateTargetBounds(tag, rect)
                    }
                )
            }
        }

        host.addView(composeView)
        overlayHostLayout = host

        try {
            windowManager?.addView(host, params)
            isOverlayAttached = true
            BubbleLog.i(TAG, "WindowManager overlay view attached successfully")
        } catch (e: Exception) {
            BubbleLog.e(TAG, "Failed to attach WindowManager overlay view", e)
        }
    }

    /**
     * Creates and attaches a discreet ongoing foreground notification with pending intent.
     */
    private fun startForegroundNotification() {
        val channelId = CHANNEL_ID
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            pendingIntentFlags
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_channel_desc))
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        BubbleLog.i(TAG, "onTaskRemoved: Ensuring overlay service remains alive")
    }

    override fun onDestroy() {
        super.onDestroy()
        BubbleLog.i(TAG, "Tearing down OverlayService and removing WindowManager view")

        overlayHostLayout?.let { host ->
            if (isOverlayAttached) {
                try {
                    windowManager?.removeView(host)
                } catch (e: Exception) {
                    BubbleLog.e(TAG, "Exception removing overlay view from WindowManager", e)
                }
                isOverlayAttached = false
            }
        }
        overlayHostLayout = null

        lifecycleOwner.onDestroy()
    }

    companion object {
        private const val TAG = "OverlayService"
        private const val CHANNEL_ID = "bubble_overlay_service_channel"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.bubble.action.START_OVERLAY"
        const val ACTION_STOP = "com.bubble.action.STOP_OVERLAY"
    }
}
