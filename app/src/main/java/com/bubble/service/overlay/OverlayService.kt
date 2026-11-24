package com.bubble.service.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.bubble.ui.overlay.BubbleOverlayContent
import com.bubble.ui.overlay.OverlayHostLayout

/**
 * Foreground service hosting the full-screen transparent WindowManager overlay.
 * Coordinates Compose view tree lifecycles and transparent touch dispatching.
 */
class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayHostLayout: OverlayHostLayout? = null
    private val lifecycleOwner = OverlayLifecycleOwner()
    private var isOverlayAttached: Boolean = false

    override fun onCreate() {
        super.onCreate()
        BubbleLog.i("OverlayService", "Initializing OverlayService and WindowManager host")

        lifecycleOwner.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        startForegroundNotification()
        attachOverlayWindow()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleOwner.onStart()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Attaches the ComposeView hierarchy to Android WindowManager.
     */
    private fun attachOverlayWindow() {
        if (isOverlayAttached) return

        // Verify system overlay permission
        if (!Settings.canDrawOverlays(this)) {
            BubbleLog.w("OverlayService", "Cannot attach overlay: SYSTEM_ALERT_WINDOW permission missing")
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
            // Bridge Jetpack Compose ViewTree lifecycle contracts
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
            BubbleLog.i("OverlayService", "WindowManager overlay view attached successfully")
        } catch (e: Exception) {
            BubbleLog.e("OverlayService", "Failed to attach WindowManager overlay view", e)
        }
    }

    /**
     * Creates and attaches a discreet ongoing foreground notification.
     */
    private fun startForegroundNotification() {
        val channelId = "bubble_overlay_service_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_channel_desc))
            .setPriority(NotificationCompat.PRIORITY_LOW)
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

    override fun onDestroy() {
        super.onDestroy()
        BubbleLog.i("OverlayService", "Tearing down OverlayService and removing WindowManager view")

        overlayHostLayout?.let { host ->
            if (isOverlayAttached) {
                try {
                    windowManager?.removeView(host)
                } catch (e: Exception) {
                    BubbleLog.e("OverlayService", "Exception removing overlay view from WindowManager", e)
                }
                isOverlayAttached = false
            }
        }
        overlayHostLayout = null

        lifecycleOwner.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
