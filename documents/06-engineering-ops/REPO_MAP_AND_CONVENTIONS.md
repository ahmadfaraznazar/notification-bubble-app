# Phase 6.1: Repository Architecture, Code Conventions & Engineering Ops

> **Module**: Notification Bubble App Engineering Operations & Quality Standards  
> **Status**: APPROVED & COMPLETE  
> **Target Toolchain**: Kotlin 2.0+, Jetpack Compose, Gradle KTS, detekt, ktlint  
> **Benchmarked Standards**: Android Open Source Project (AOSP), Signal-Android, Lawnchair, NewPipe  

---

## 1. Executive Summary & Core Engineering Tenets

A mission-critical system overlay operating continuously in the background requires strict engineering standards. Memory leaks trigger OS task-killer terminations, unconstrained pointer math drops frames on 120Hz displays, and untracked logging can inadvertently expose private notification data to system logcat.

This document establishes the **5 Engineering Tenets**, complete directory architecture, coding conventions, structured logging protocols, and automated CI quality gates.

```
┌─────────────────────────────────────────────────────────────────┐
│                     CORE ENGINEERING TENETS                     │
├─────────────────┬─────────────────┬──────────────┬──────────────┤
│ 1. Zero Heap    │ 2. Domain Layer │ 3. Redacted  │ 4. Strict    │
│    Allocation   │    Isolation    │    Logcat    │    Size Gate │
│ Zero garbage    │ Pure Kotlin     │ Zero sender  │ APK size     │
│ in 120Hz touch  │ zero android.*  │ names or OTPs│ strictly     │
│ drag loops      │ imports allowed │ in log stream│ under 5.0MB  │
├─────────────────┴─────────────────┴──────────────┴──────────────┤
│ 5. Human-Readable Git Commits with Explicit Confirmation         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Repository Architecture & Directory Map

The repository adheres to **Clean Architecture** organized by feature and architectural layer. The project root is clean, modular, and strictly segregated:

```text
notification-bubble-app/
├── .agents/                               # Antigravity agent skills configuration
│   └── skills.json
├── .github/
│   └── workflows/
│       ├── pr-verification.yml           # CI: lint, detekt, unit tests, size gate
│       └── release-build.yml             # AAB generation & signature packaging
├── ai-skills/                             # 21 specialized engineering lifecycle skills
├── documents/                             # Complete 6-phase engineering specifications
│   ├── PROJECT_PROGRESS_TRACKER.md        # Master lifecycle tracking dashboard
│   ├── PRODUCT_INTAKE_QUESTIONNAIRE.md    # Product vision & user preferences
│   ├── 01-ideation-and-validation/        # Phase 1 Strategy Artifacts
│   ├── 02-planning-and-pm/                # Phase 2 Specifications (PRD, Tasks)
│   ├── 03-architecture-and-tech/          # Phase 3 Blueprints (Clean Arch, Room, Best Practices)
│   ├── 04-design-and-ux/                  # Phase 4 Visual Specs (Tokens, Physics, A11y)
│   ├── 05-growth-and-monetization/        # Phase 5 Commercial Specs (IAP, ASO, Copywriting)
│   └── 06-engineering-ops/                # Phase 6 Operational Specs (Conventions, PR Audit)
├── scripts/
│   ├── pre-commit-check.sh               # Local pre-commit verification script
│   └── verify-zero-network.sh            # Manifest audit script (verifies no INTERNET permission)
├── AGENTS.md                              # Mandatory project rules & git conventions
├── README.md                              # Public repository documentation
├── build.gradle.kts                       # Root build script
├── settings.gradle.kts                    # Module definitions
└── app/
    ├── build.gradle.kts                   # App-level build script (dependencies, R8 config)
    ├── proguard-rules.pro                 # R8 optimization & log-stripping rules
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml        # Zero-network manifest
        │   ├── java/com/bubble/
        │   │   ├── core/                  # Shared utilities, dispatchers, error models
        │   │   │   ├── dispatcher/        # Coroutine dispatcher abstractions
        │   │   │   ├── logging/           # Redacted structured logger (BubbleLog)
        │   │   │   └── result/            # Typed Result<T> monads
        │   │   ├── domain/                # Pure Kotlin domain layer (ZERO android.*)
        │   │   │   ├── model/             # ActiveBubble, NotificationMessage, AppRule
        │   │   │   ├── repository/        # NotificationRepository, RuleRepository
        │   │   │   └── usecase/           # IngestNotificationUseCase, DismissBubbleUseCase
        │   │   ├── data/                  # Data layer (Room SQLite, DataStore, Billing)
        │   │   │   ├── local/             # Room Database, DAOs, Entities
        │   │   │   │   ├── dao/           # AppRuleDao, NotificationDao
        │   │   │   │   ├── entity/        # AppRuleEntity, UserPreferencesEntity
        │   │   │   │   └── AppDatabase.kt
        │   │   │   ├── billing/           # BillingManager (Google Play Billing v7)
        │   │   │   └── repository/        # NotificationRepositoryImpl, RuleRepositoryImpl
        │   │   ├── service/               # Android System Services
        │   │   │   ├── listener/          # NotificationListener (Status bar interception)
        │   │   │   ├── overlay/           # OverlayService (Foreground WindowManager host)
        │   │   │   └── receiver/          # BootReceiver (Auto-restart on reboot)
        │   │   └── ui/                    # Jetpack Compose UI layer
        │   │       ├── theme/             # Tokens, typography, colors, BubbleTheme
        │   │       ├── atoms/             # AvatarCircle, AppBadgeIcon, UnreadCounterBadge
        │   │       ├── molecules/         # FloatingBubbleItem, ActionRow, PreviewHeader
        │   │       ├── organisms/         # NotificationPreviewCard, DismissTargetZone, BubbleStack
        │   │       ├── interaction/       # BubblePhysicsController, ImeAvoidanceModifier
        │   │       └── screens/           # OnboardingWizard, SettingsScreen, PaywallSheet
        │   └── res/                       # Vector XML assets, values, mipmap
        ├── test/                          # Unit tests (JVM: Robolectric, MockK, Coroutines Test)
        └── androidTest/                   # Instrumented tests (Compose UI Test Rule)
```

---

## 3. Naming Standards & Code Conventions

All Kotlin code strictly conforms to the [Official Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) with project-specific reinforcements:

### 3.1 Naming Rules Matrix

| Construct | Case / Pattern | Examples | Anti-Patterns (Forbidden) |
|---|---|---|---|
| **Classes & Interfaces** | `PascalCase` | `BubblePhysicsController`, `NotificationRepository` | `bubble_controller`, `INotificationRepo` |
| **Composables** | `PascalCase` (Noun) | `AvatarCircle()`, `DismissTargetZone()` | `renderAvatar()`, `avatarCircle()` |
| **Functions** | `camelCase` (Verb-noun) | `dismissActiveBubble()`, `calculateSnapTarget()` | `bubble()`, `DoDismiss()` |
| **Properties & Variables** | `camelCase` (Descriptive) | `isProUnlocked`, `unreadMessageCount` | `flag`, `temp`, `data`, `x` |
| **Constants** | `SCREAMING_SNAKE` | `DEFAULT_BUBBLE_SIZE_DP`, `MAX_VISIBLE_HEADS` | `defaultSize`, `kMaxHeads` |
| **Room Entities** | `PascalCase` + `Entity` | `AppRuleEntity`, `UserPreferencesEntity` | `AppRuleTable`, `rules` |
| **Room DAOs** | `PascalCase` + `Dao` | `AppRuleDao`, `NotificationHistoryDao` | `RuleQueries` |
| **Test Classes** | `SubjectUnderTest` + `Test` | `BubblePhysicsControllerTest`, `AppRuleDaoTest` | `TestPhysics` |
| **Test Functions** | `whenCondition_expectedResult` | `whenDragExceedsThreshold_snapsToRightEdge()` | `test1()`, `testDrag()` |

### 3.2 Immutability & Compose Stability Law
- All data models in the `domain` and `ui` packages MUST be immutable (`data class` with `val` properties).
- Never mutate collections directly. Use `.toPersistentList()` from `kotlinx.collections.immutable` or standard functional transformations (`+`, `-`, `.map`).
- Avoid passing raw lambdas into composables if they capture unstable scope; prefer method references or stable hoisted events.

---

## 4. Structured Logging Standards & Zero-Leakage Policy

Because this application intercepts sensitive personal communications (SMS, WhatsApp, Telegram, banking alerts), **standard unfiltered `Log.d()` is strictly forbidden**.

```
┌─────────────────────────────────────────────────────────────┐
│                 LOGGING SAFETY BOUNDARIES                   │
├───────────────────────────────┬─────────────────────────────┤
│ FORBIDDEN IN ALL LOGS         │ PERMITTED IN DEBUG LOGS     │
│ ❌ Message body / preview text │ ✓ Originating package name  │
│ ❌ Sender name or phone number │ ✓ Notification category ID  │
│ ❌ Banking / OTP digits       │ ✓ Message body byte length  │
│ ❌ User contacts / addresses  │ ✓ Touch coordinates & fps   │
└───────────────────────────────┴─────────────────────────────┘
```

### 4.1 Production Redacted Logger (`BubbleLog.kt`)

```kotlin
package com.bubble.core.logging

import android.util.Log

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
     * Logs notification arrival safely without exposing private content.
     */
    fun logNotificationReceived(packageName: String, bodyLength: Int, isMasked: Boolean) {
        d("Notification", "Intercepted: pkg=$packageName, bytes=$bodyLength, masked=$isMasked")
    }
}
```

### 4.2 ProGuard Release Stripping
In release builds, R8 completely strips debug log invocations to guarantee zero performance overhead and zero log leakage:

```proguard
# proguard-rules.pro
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
```

---

## 5. Git Workflow & Mandatory Commit Protocols

In strict accordance with [`AGENTS.md`](file:///Users/ahmadnazar/Documents/GitHub/notification-bubble-app/AGENTS.md):

### 5.1 The 3 Golden Git Rules
1. **Explicit User Confirmation Required**: Never commit without presenting staged files and receiving explicit user approval.
2. **Forbidden Prefixes**: Never use conventional commit prefixes (`feat:`, `fix:`, `chore:`, `refactor:`, `style:`, `test:`, `ci:`).
3. **Format Requirement**: Always start with a capitalized contextual header followed by a concise, human-readable, non-technical message in plain English.

### 5.2 Authorized Commit Header Examples
- `Initial Commit: add ai skills library`
- `UI Update: adjust notification bubble drag physics and edge snapping`
- `Feature: support inline quick reply for chat notifications`
- `Bug Fix: resolve issue where overlay blocked on-screen keyboard`
- `Architecture: add room database schema and data modeling specification`
- `Design: add persistent design system specification and update progress tracker`
- `Distribution: add go to market distribution strategy and app store optimization package`
- `Marketing: add market positioning angles copywriting and complete phase 5`
- `Configuration: define human-readable commit message conventions`

---

## 6. Automated CI Quality Gates & Verification Scripts

Every Pull Request must pass 4 automated verification gates before merging:

```
[ Gate 1: Code Style (ktlint) ] ──► [ Gate 2: Static Analysis (detekt) ]
                │                                    │
                ▼                                    ▼
[ Gate 3: Unit Tests (testDebugUnitTest) ] ──► [ Gate 4: Zero-Network Audit ]
```

### 6.1 Pre-Commit Local Verification Script (`scripts/pre-commit-check.sh`)

```bash
#!/usr/bin/env bash
set -e

echo "=== [1/4] Running ktlint format check ==="
./gradlew ktlintCheck --daemon

echo "=== [2/4] Running detekt static code analysis ==="
./gradlew detekt --daemon

echo "=== [3/4] Executing JVM Unit Tests ==="
./gradlew testDebugUnitTest --daemon

echo "=== [4/4] Verifying Zero-Network Manifest Policy ==="
bash scripts/verify-zero-network.sh

echo "✅ All local verification gates passed successfully!"
```

### 6.2 Zero-Network Manifest Verification Script (`scripts/verify-zero-network.sh`)

```bash
#!/usr/bin/env bash
set -e

MANIFEST_FILE="app/src/main/AndroidManifest.xml"

if [ -f "$MANIFEST_FILE" ]; then
    if grep -q "android.permission.INTERNET" "$MANIFEST_FILE"; then
        echo "❌ CRITICAL SECURITY VIOLATION: android.permission.INTERNET found in $MANIFEST_FILE!"
        exit 1
    else
        echo "✅ Verified: Zero-network policy intact. No INTERNET permission in manifest."
    fi
else
    echo "⚠️ Warning: Manifest file not yet generated. Gate skipped during planning phases."
fi
```

### 6.3 APK Binary Size Budget Gate
- **Maximum Compressed APK Size**: Strictly $\le 5.0\text{MB}$.
- Enforced via GitHub Actions release workflow:
  ```bash
  APK_SIZE=$(stat -c%s app/build/outputs/apk/release/app-release-unsigned.apk 2>/dev/null || stat -f%z app/build/outputs/apk/release/app-release-unsigned.apk)
  MAX_SIZE=5242880 # 5.0 MB in bytes
  if [ "$APK_SIZE" -gt "$MAX_SIZE" ]; then
      echo "❌ Build exceeded 5MB APK size budget! Actual: $APK_SIZE bytes"
      exit 1
  fi
  ```

---

## 7. Verification & Quality Checklist

- [x] Full repository architecture and package structure mapped from `core/` to `ui/`.
- [x] Naming standards defined for Kotlin classes, Composables, functions, and Room entities.
- [x] Redacted logging protocol (`BubbleLog`) established with release stripping in ProGuard.
- [x] Git workflow and human-readable commit rules codified with explicit user confirmation.
- [x] Pre-commit shell scripts and manifest zero-network verification gates specified.
- [x] Sub-5MB binary size budget codified with CI build failure thresholds.
