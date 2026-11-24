package com.bubble.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.bubble.core.logging.BubbleLog
import com.bubble.service.overlay.OverlayService

/**
 * BroadcastReceiver responsible for reviving the overlay service upon device boot or app update.
 * Ensures floating conversation heads survive phone restarts without manual user intervention.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        BubbleLog.i("BootReceiver", "Received broadcast action: $action")

        val isBoot = action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED

        if (isBoot) {
            // Verify overlay permission before attempting to launch foreground service
            if (Settings.canDrawOverlays(context)) {
                BubbleLog.i("BootReceiver", "Overlay permission granted. Restarting OverlayService.")
                val serviceIntent = Intent(context, OverlayService::class.java).apply {
                    this.action = OverlayService.ACTION_START
                }

                try {
                    ContextCompat.startForegroundService(context, serviceIntent)
                } catch (e: Exception) {
                    BubbleLog.e("BootReceiver", "Failed to start OverlayService on boot", e)
                }
            } else {
                BubbleLog.w("BootReceiver", "Overlay permission not granted. Skipping service revival.")
            }
        }
    }
}
