package com.bubble.service.oem

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.bubble.core.logging.BubbleLog

enum class OEMBrand(val displayName: String) {
    SAMSUNG("Samsung"),
    XIAOMI("Xiaomi / HyperOS"),
    HUAWEI("Huawei"),
    OPPO_ONEPLUS("OnePlus / Oppo"),
    VIVO("Vivo"),
    GENERIC("Android System")
}

/**
 * Detects device manufacturer and resolves vendor-specific autostart & battery
 * exemption intents (dontkillmyapp.com patterns) with graceful system fallbacks.
 */
object OEMExemptionHelper {

    private const val TAG = "OEMExemptionHelper"

    /**
     * Identifies the OEM brand from Build.MANUFACTURER.
     */
    fun detectBrand(manufacturer: String = Build.MANUFACTURER.orEmpty()): OEMBrand {
        val normalized = manufacturer.trim().lowercase()
        return when {
            normalized.contains("samsung") -> OEMBrand.SAMSUNG
            normalized.contains("xiaomi") || normalized.contains("redmi") || normalized.contains("poco") -> OEMBrand.XIAOMI
            normalized.contains("huawei") || normalized.contains("honor") -> OEMBrand.HUAWEI
            normalized.contains("oneplus") || normalized.contains("oppo") || normalized.contains("realme") -> OEMBrand.OPPO_ONEPLUS
            normalized.contains("vivo") || normalized.contains("iqoo") -> OEMBrand.VIVO
            else -> OEMBrand.GENERIC
        }
    }

    /**
     * Checks whether the app is currently whitelisted from OS battery optimizations.
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } catch (e: Exception) {
            BubbleLog.w(TAG, "Failed to query PowerManager battery optimization status", e)
            false
        }
    }

    /**
     * Returns a prioritized list of OEM autostart / battery exemption intents.
     */
    fun getCandidateIntents(context: Context, brand: OEMBrand): List<Intent> {
        val pkg = context.packageName
        val candidates = mutableListOf<Intent>()

        when (brand) {
            OEMBrand.SAMSUNG -> {
                // Samsung Device Care / Battery
                candidates.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.samsung.android.lool",
                            "com.samsung.android.sm.ui.battery.BatteryActivity"
                        )
                    )
                )
                candidates.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.samsung.android.sm",
                            "com.samsung.android.sm.ui.battery.BatteryActivity"
                        )
                    )
                )
            }
            OEMBrand.XIAOMI -> {
                // MIUI / HyperOS Security AutoStart & PowerKeeper
                candidates.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
                    )
                )
                candidates.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.miui.powerkeeper",
                            "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
                        )
                    ).putExtra("package_name", pkg).putExtra("package_label", "Notification Bubble")
                )
            }
            OEMBrand.HUAWEI -> {
                // Huawei Phone Manager Protect / Startup
                candidates.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.optimize.process.ProtectActivity"
                        )
                    )
                )
                candidates.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
                        )
                    )
                )
            }
            OEMBrand.OPPO_ONEPLUS -> {
                // ColorOS / OxygenOS Startup & SafeCenter
                candidates.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                        )
                    )
                )
                candidates.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.oplus.battery",
                            "com.oplus.powermanager.fuelgaue.PowerUsageModelActivity"
                        )
                    )
                )
            }
            OEMBrand.VIVO -> {
                // Vivo iQOO Whitelist / Startup
                candidates.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                        )
                    )
                )
                candidates.add(
                    Intent().setComponent(
                        ComponentName(
                            "com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                        )
                    )
                )
            }
            OEMBrand.GENERIC -> {
                // Handled by universal fallbacks below
            }
        }

        // Universal Android Fallbacks (Standard Battery & App Details)
        candidates.add(
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:$pkg")
            )
        )
        candidates.add(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:$pkg")
            )
        )
        candidates.add(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))

        return candidates
    }

    /**
     * Safely attempts to launch the most specific OEM battery settings activity,
     * gracefully falling back to standard Android battery settings.
     */
    fun launchExemptionSettings(context: Context): Boolean {
        val brand = detectBrand()
        val intents = getCandidateIntents(context, brand)

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Check if an activity can resolve this intent before starting
                val resolved = context.packageManager.queryIntentActivities(intent, 0)
                if (resolved.isNotEmpty()) {
                    context.startActivity(intent)
                    BubbleLog.i(TAG, "Successfully launched battery settings for ${brand.displayName} via ${intent.component ?: intent.action}")
                    return true
                }
            } catch (e: Exception) {
                BubbleLog.d(TAG, "Intent candidate failed: ${intent.component ?: intent.action}, trying fallback...")
            }
        }

        // Final safe fallback if queryIntentActivities returned empty
        return try {
            val fallback = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallback)
            BubbleLog.i(TAG, "Launched application details fallback settings")
            true
        } catch (e: Exception) {
            BubbleLog.e(TAG, "All battery exemption intents failed", e)
            false
        }
    }
}
