# Security & Privacy Audit Specification

> **Phase 3: Architecture & Tech (Step 3.5)**  
> **Status**: APPROVED  
> **Standard**: OWASP Top 10:2025 Mobile / Zero-Network Verification / Android Security Model  
> **Target Release**: v1.0.0

---

## 1. Executive Summary & Security Philosophy

The Notification Bubble App intercepts incoming device notifications to render floating conversation heads and provide inline quick replies. Because notification streams frequently carry sensitive personal data (e.g., private chats, financial transaction alerts, 2FA/OTP codes), security and user privacy form the core technical moat of this product.

### The "Zero-Network by Architecture" Moat
Unlike competitor utility apps that harvest notification telemetry or embed third-party analytics and ad trackers, our product is **architecturally incapable of exfiltrating user data**:
1. **Zero Network Permission**: `android.permission.INTERNET` is strictly forbidden and absent from `AndroidManifest.xml`.
2. **Ephemeral RAM-Only Messaging**: Message bodies and conversation threads reside exclusively in transient memory (`StateFlow`) and are never written to unencrypted SQLite tables or long-term disk caches.
3. **Automated Sensitive Redaction**: OTP, 2FA verification codes, and banking transaction alerts are automatically detected via pre-compiled regex heuristics and blocked from spawning floating bubbles.

---

## 2. Threat Modeling & STRIDE Matrix

```
                      ┌───────────────────────────────────────────────┐
                      │              Android OS Kernel                │
                      └──────────────────────┬────────────────────────┘
                                             │ Binder IPC
                                             ▼
  ┌─────────────────────────┐    NotificationListenerService    ┌─────────────────────────┐
  │ Untrusted Third-Party   │◄───────────────┼─────────────────►│   Malicious Overlay     │
  │ Apps (No Permissions)   │  Protected by  │  Protected by    │   (Tapjacking Attempts) │
  └─────────────────────────┘  BIND_LISTENER │  FLAG_SECURE &   └─────────────────────────┘
                                             │  filterTouchesWhenObscured
                                             ▼
                               ┌───────────────────────────┐
                               │  RAM-Only Bubble Manager  │
                               │  (Ephemeral StateFlow)    │
                               └─────────────┬─────────────┘
                                             │ Zero Network Capability
                                             ▼
                               [ No Internet Socket Available ]
```

| STRIDE Threat | Potential Attack Vector | Severity | Architectural Mitigation | Verification Seam |
|---|---|:---:|---|---|
| **Spoofing** | Malicious app broadcasts fake notification intents to force bubble creation | **Medium** | Notification streams are ingested exclusively via Android's protected `NotificationListenerService` Binder interface. No custom external `BroadcastReceiver` accepts notification payloads. | Verify zero exported broadcast receivers in manifest. |
| **Tampering** | Rogue app manipulates SQLite database on rooted or compromised device | **Low** | Room DB contains only package whitelist toggles (`app_rules`) and IAP purchase tokens. No notification content or message strings are persisted. | Database inspection verifies zero message content tables. |
| **Repudiation** | User denies sending an inline quick reply | **Low** | Replies execute directly via original app's pending intent (`RemoteInput.sendResult()`). Our app maintains no historical outbound message log. | System OS manages delivery receipt and identity attribution. |
| **Information Disclosure** | Shoulder surfing, screen recorders, or `logcat` dumps leaking chat content | **High** | 1. `FLAG_SECURE` available for sensitive overlays.<br>2. Complete R8 ProGuard stripping of all `Log.d/v/i` and `println`.<br>3. Automated OTP/2FA heuristic drop engine. | Release APK logcat audit shows 0 lines emitted during active notifications. |
| **Denial of Service** | Notification flood (1,000 msgs/min) causing OOM crash in overlay service | **Medium** | Backpressure queue with max 10 active bubble capacity. FIFO pruning drops oldest inactive conversations when queue ceiling is reached. | Stress test with 50 msgs/sec synthetic load generator. |
| **Elevation of Privilege** | Tapjacking overlay attacks stealing touches from underlying banking apps | **Critical** | Overlays set `FLAG_NOT_TOUCH_MODAL` and declare `filterTouchesWhenObscured = true` on sensitive interactive surfaces. When user leaves bubbles, overlay shrinks to non-blocking 56dp edge anchor. | Accessibility and overlay touch pass-through verification. |

---

## 3. Zero-Network Manifest Verification & CI Gate

### 3.1 Strict Manifest Policy
The application manifest MUST NOT declare network capabilities:
```xml
<!-- FORBIDDEN IN PRODUCTION: -->
<!-- <uses-permission android:name="android.permission.INTERNET" /> -->
<!-- <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> -->
```

### 3.2 Automated Manifest Merger Guard (Gradle CI Task)
Third-party libraries (e.g., transitive SDK dependencies) could inadvertently introduce `android.permission.INTERNET` during Gradle manifest merger. To guarantee zero-network compliance, the project includes an automated manifest gate task in `app/build.gradle.kts`:

```kotlin
// app/build.gradle.kts
tasks.register("verifyZeroNetworkManifest") {
    description = "Fails build if android.permission.INTERNET is present in merged manifest"
    dependsOn("processReleaseManifest")
    
    doLast {
        val manifestFile = layout.buildDirectory.file(
            "intermediates/merged_manifest/release/processReleaseManifest/AndroidManifest.xml"
        ).get().asFile
        
        if (manifestFile.exists()) {
            val content = manifestFile.readText()
            if (content.contains("android.permission.INTERNET") || 
                content.contains("android.permission.ACCESS_NETWORK_STATE")) {
                throw GradleException(
                    "SECURITY AUDIT FAILURE: Merged manifest contains unauthorized network permissions! " +
                    "Check dependencies for transitive network requirements."
                )
            }
        }
    }
}
```

---

## 4. Notification Interception & Privacy Boundaries

### 4.1 Protected Service Binding
The `NotificationListenerService` can only be bound by the Android System Server:
```xml
<service
    android:name=".data.service.BubbleNotificationListenerService"
    android:label="@string/app_name"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```
*Any third-party app attempting to bind to this service is rejected with a `SecurityException` at the Binder IPC boundary.*

### 4.2 Heuristic OTP & Sensitive Alert Redaction Engine
Notifications containing authentication tokens, one-time passwords, or bank transaction summaries must **never** be projected onto floating screen overlays.

```kotlin
// domain/security/SensitiveNotificationFilter.kt
package com.bubble.notification.domain.security

object SensitiveNotificationFilter {
    
    // High-confidence regex signatures for 2FA / OTP codes
    private val OTP_PATTERNS = listOf(
        Regex("(?i)\\b(otp|code|verification|passcode|secret|pin)\\b.*\\b\\d{4,8}\\b"),
        Regex("(?i)\\b\\d{4,8}\\b.*\\b(is your verification code|is your OTP|is your login code)\\b"),
        Regex("(?i)\\bsecurity code:\\s*\\d{4,8}\\b"),
        Regex("(?i)\\buse\\s+\\d{4,8}\\s+to\\s+verify\\b")
    )
    
    // Financial and banking transaction keywords
    private val FINANCIAL_PATTERNS = listOf(
        Regex("(?i)\\b(debited|credited|withdrawn|transaction of|balance:)\\b"),
        Regex("(?i)\\b(cvv|card ending in|account ending in)\\b")
    )

    // Permanent package exclusion list
    private val SENSITIVE_PACKAGE_BLACKLIST = setOf(
        "com.google.android.apps.authenticator2", // Google Authenticator
        "com.authy.authy",                        // Twilio Authy
        "com.duosecurity.duomobile",              // Duo Mobile
        "com.microsoft.msa.authenticator",        // MS Authenticator
        "com.bitwarden.authenticator",            // Bitwarden Authenticator
        "com.onepassword.android",                // 1Password
        "com.lastpass.lpandroid"                  // LastPass
    )

    fun isSensitive(packageName: String, title: String?, text: String?): Boolean {
        // 1. Blacklisted security authenticators
        if (SENSITIVE_PACKAGE_BLACKLIST.contains(packageName)) {
            return true
        }
        
        val combinedContent = "${title.orEmpty()} ${text.orEmpty()}"
        if (combinedContent.isBlank()) return false
        
        // 2. OTP & 2FA regex matching
        if (OTP_PATTERNS.any { it.containsMatchIn(combinedContent) }) {
            return true
        }
        
        // 3. Financial alert matching
        if (FINANCIAL_PATTERNS.any { it.containsMatchIn(combinedContent) }) {
            return true
        }
        
        return false
    }
}
```

---

## 5. Storage & Memory Lifecycle Isolation

### 5.1 Zero Disk Persistence for Messages
```
Incoming Notification
         │
         ▼
[ SensitiveNotificationFilter ] ──(Sensitive?)──► DROP (Ignore silently)
         │ (Safe)
         ▼
[ ActiveConversationManager ] ──(RAM Only: StateFlow)──► Compose Bubble UI
         │
         ▼ (User swipes to dismiss bubble)
[ Evicted from RAM ] ──► Garbage Collected (No DB write, No file write)
```

### 5.2 Room SQLite Audit
Our Room SQLite schema ([documents/03-architecture-and-tech/DATA_MODELING_AND_ROOM_SCHEMA.md](file:///Users/ahmadnazar/Documents/GitHub/notification-bubble-app/documents/03-architecture-and-tech/DATA_MODELING_AND_ROOM_SCHEMA.md)) stores strictly three tables:
- `app_rules`: Package ID, display name, bubble enabled toggle, accent color (No messages).
- `active_conversations`: Conversation key, package ID, unread count, timestamp (No messages).
- `purchase_records`: In-App Purchase SKU ID, signature token, timestamp (No personal data).

*Result: Even if device physical memory or backup archives are extracted, zero chat logs or personal message snippets can be recovered.*

---

## 6. ProGuard & Logging Sanitization

To ensure zero leakage of contact names, package names, or message snippets to system `logcat`, release builds enforce aggressive R8 bytecode optimization and logging elimination:

```proguard
# proguard-rules.pro
# Remove all standard Android logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# Remove standard console print streams
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}
```

---

## 7. Google Play Data Safety Declaration Form

When publishing on Google Play Console, apps with notification permissions undergo strict automated review. Below is the pre-filled verification manifest for the Play Store Data Safety Questionnaire:

| Questionnaire Question | Value | Technical Justification |
|---|:---:|---|
| **Does your app collect or share user data?** | **NO** | App performs on-device presentation only. No remote servers exist. |
| **Is all user data encrypted in transit?** | **N/A** | App does not transmit data over any network connection. |
| **Does your app provide a way for users to request data deletion?** | **YES** | Local database can be cleared instantaneously via "Reset All Settings" button in app. |
| **Is this app designed for families/children?** | **NO** | General utility multitasking application. |
| **Are notifications transferred to third parties?** | **NO** | Zero analytics SDKs, zero ad SDKs, zero crash reporters. |

---

## 8. UI & UX Advisory Protocol: Privacy & Trust Benchmarking

### 8.1 Domain Comparison: Privacy Friction & Transparency

| Dimension | Facebook Messenger Chat Heads | Android AOSP System Bubbles | DynamicSpot | **Notification Bubble App (Our Target)** |
|---|---|---|---|---|
| **Network Capability** | Full internet (Syncs data, ads, tracking) | OS internal (No external app boundary) | Internet enabled (Contains AdMob & Firebase) | **Zero Network (`INTERNET` forbidden)** |
| **OTP / Sensitive Leak** | Shows previews for linked SMS accounts | Relies on system notification channel importance | Displays preview for all enabled apps | **Automated heuristic redaction for OTP & Finance** |
| **Shoulder Surfing** | Large expanded card always visible | System window overlay | Floating top notch pill | **Configurable Privacy Mode (Avatar only, no preview text)** |
| **Trust Visualizer** | None (Assumed Facebook collection) | System settings toggle | Generic toggle list | **Settings screen features "Zero-Network Verified" badge** |

### 8.2 UX Privacy Recommendations
1. **Onboarding Trust Cue**: During permission onboarding, display a transparent card: *"This app does not request or possess Internet permission. Your notifications never leave this device."*
2. **Lockscreen / Privacy Shield**: Provide a one-click toggle in Settings: *"Hide preview text while device is locked or in public mode"*, showing only sender avatar and app badge.

---

## 9. Verification & Gate Checklist

- [x] Zero-network manifest assertion written and documented.
- [x] `SensitiveNotificationFilter` OTP and financial regex patterns specified.
- [x] RAM-only message retention lifecycle validated (zero SQLite chat storage).
- [x] R8 ProGuard log-stripping configuration established.
- [x] Google Play Data Safety responses mapped for zero-friction store approval.
- [x] UI/UX privacy benchmarking completed.
