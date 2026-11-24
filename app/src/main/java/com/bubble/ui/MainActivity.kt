package com.bubble.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bubble.service.oem.OEMExemptionHelper
import com.bubble.service.overlay.OverlayService
import com.bubble.ui.onboarding.AppPickerScreen
import com.bubble.ui.onboarding.AppPickerViewModel
import com.bubble.ui.onboarding.MascotWizardScreen
import com.bubble.ui.onboarding.OEMSetupDialog
import com.bubble.ui.onboarding.checkNotificationListenerAccess
import com.bubble.ui.onboarding.checkOverlayPermission

enum class NavigationScreen {
    MASCOT_ONBOARDING,
    DASHBOARD,
    APP_PICKER
}

/**
 * Main application dashboard and onboarding entry point.
 * Guides users through permission grants, service controls, and app whitelisting.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appPickerViewModel = AppPickerViewModel.create(this)

        val isFirstLaunch = !checkOverlayPermission(this) || !checkNotificationListenerAccess(this)
        val initialScreen = if (isFirstLaunch) NavigationScreen.MASCOT_ONBOARDING else NavigationScreen.DASHBOARD

        setContent {
            var currentScreen by remember { mutableStateOf(initialScreen) }

            when (currentScreen) {
                NavigationScreen.MASCOT_ONBOARDING -> {
                    MascotWizardScreen(
                        onFinishOnboarding = {
                            currentScreen = NavigationScreen.APP_PICKER
                        }
                    )
                }
                NavigationScreen.DASHBOARD -> {
                    BubbleAppDashboard(
                        appPickerViewModel = appPickerViewModel,
                        onNavigateToAppPicker = { currentScreen = NavigationScreen.APP_PICKER },
                        onNavigateToWizard = { currentScreen = NavigationScreen.MASCOT_ONBOARDING }
                    )
                }
                NavigationScreen.APP_PICKER -> {
                    AppPickerScreen(
                        viewModel = appPickerViewModel,
                        onNavigateBack = { currentScreen = NavigationScreen.DASHBOARD }
                    )
                }
            }
        }
    }
}

@Composable
fun BubbleAppDashboard(
    appPickerViewModel: AppPickerViewModel,
    onNavigateToAppPicker: () -> Unit,
    onNavigateToWizard: () -> Unit = {}
) {
    val context = LocalContext.current
    val hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    val isBatteryOptimized by remember { mutableStateOf(OEMExemptionHelper.isBatteryOptimizationIgnored(context)) }
    val brand = remember { OEMExemptionHelper.detectBrand() }
    var showOemDialog by remember { mutableStateOf(false) }
    val pickerUiState by appPickerViewModel.uiState.collectAsState()

    if (showOemDialog) {
        OEMSetupDialog(onDismiss = { showOemDialog = false })
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A) // Dark Navy Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Section
            Text(
                text = "Notification Bubble",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Persistent floating heads for your favorite messaging apps with zero network telemetry.",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Privacy Moat Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🛡️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Zero-Network Guarantee",
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF38BDF8),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "This app has zero internet permissions. Your chats and notifications never leave this device.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Permission Card: Draw Over Other Apps
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overlay Permission",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        StatusPill(isGranted = hasOverlayPermission)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Required to render floating conversation heads above active games and applications.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    if (!hasOverlayPermission) {
                        Button(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            // OEM Battery Survival Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Battery Optimization",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        StatusPill(isGranted = isBatteryOptimized)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Prevent ${brand.displayName} task killers from stopping chat heads when the screen locks.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showOemDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Battery Survival Guide")
                    }
                }
            }

            // App Whitelist Configuration Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "App Whitelist",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0369A1),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${pickerUiState.enabledCount} enabled",
                                color = Color(0xFFE0F2FE),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Choose which messaging and chat apps are authorized to spawn floating bubbles.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToAppPicker,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Configure Apps")
                    }
                }
            }

            // Service Controls Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Overlay Service Engine",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start the resilient foreground overlay service to display floating bubbles.",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(context, OverlayService::class.java).apply {
                                    action = OverlayService.ACTION_START
                                }
                                ContextCompat.startForegroundService(context, intent)
                            },
                            enabled = hasOverlayPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Start Service")
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(context, OverlayService::class.java).apply {
                                    action = OverlayService.ACTION_STOP
                                }
                                context.startService(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Stop Service", color = Color(0xFFF87171))
                        }
                    }
                }
            }

            // Onboarding Wizard Replay Link
            OutlinedButton(
                onClick = onNavigateToWizard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Replay Mascot Onboarding Guide", color = Color(0xFF38BDF8))
            }
        }
    }
}

@Composable
fun StatusPill(isGranted: Boolean) {
    val bgColor = if (isGranted) Color(0xFF065F46) else Color(0xFF7F1D1D)
    val textColor = if (isGranted) Color(0xFF34D399) else Color(0xFFFCA5A5)
    val text = if (isGranted) "Granted" else "Required"

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
