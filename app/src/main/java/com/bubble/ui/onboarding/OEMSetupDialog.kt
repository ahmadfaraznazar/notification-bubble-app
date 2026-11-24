package com.bubble.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bubble.service.oem.OEMBrand
import com.bubble.service.oem.OEMExemptionHelper

/**
 * Dialog guiding users through OEM-specific battery restriction exemptions
 * (e.g., Samsung Device Care, Xiaomi Autostart, OnePlus Background freeze).
 */
@Composable
fun OEMSetupDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val brand = remember { OEMExemptionHelper.detectBrand() }
    var isIgnored by remember { mutableStateOf(OEMExemptionHelper.isBatteryOptimizationIgnored(context)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E293B), // Slate-800
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0369A1)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚡", fontSize = 26.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Keep Bubbles Alive",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "Your ${brand.displayName} phone has aggressive background task killers that stop floating heads when the screen turns off.",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Status & Instructions Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Battery Status",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            StatusBadge(isUnrestricted = isIgnored)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "To guarantee 100% background delivery:",
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        InstructionStep(number = "1", text = "Tap the button below to open battery settings.")
                        InstructionStep(number = "2", text = "Select Notification Bubble.")
                        InstructionStep(number = "3", text = "Set to 'Unrestricted' or enable 'Autostart'.")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = {
                        OEMExemptionHelper.launchExemptionSettings(context)
                        isIgnored = OEMExemptionHelper.isBatteryOptimizationIgnored(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Open ${brand.displayName} Settings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(text = "I'll Do This Later", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun InstructionStep(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$number. ",
            color = Color(0xFF38BDF8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            color = Color(0xFFCBD5E1),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun StatusBadge(isUnrestricted: Boolean) {
    val bgColor = if (isUnrestricted) Color(0xFF065F46) else Color(0xFF7F1D1D)
    val textColor = if (isUnrestricted) Color(0xFF34D399) else Color(0xFFFCA5A5)
    val text = if (isUnrestricted) "Unrestricted ✓" else "Restricted"

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
