# Spec-Driven Developer Task Cards: Notification Bubble App

> **Objective**: Rigorous technical specification and atomic, testable developer task cards structured by Clean Architecture and Atomic UI design.  
> **Skill Activated**: `spec-driven-tasks` ([SKILL.md](file:///Users/ahmadnazar/Documents/GitHub/notification-bubble-app/ai-skills/01-app-development/02-planning-and-pm/spec-driven-tasks/SKILL.md))  
> **Status**: Ready for Implementation (`ready-for-agent`)  
> **Date**: 2026-11-22  

---

## 1. Specification & Technical Contract

### A. Core Assumptions
1. **Platform**: Pure native Android (Kotlin 2.0+ & Jetpack Compose BOM 2024+).
2. **Minimum SDK**: Android 10 (API 29) &mdash; covers > 92% of active global Android devices.
3. **Target SDK**: Android 15 (API 35) &mdash; fully compliant with Google Play 2026 standards.
4. **Network Capability**: Zero network (`android.permission.INTERNET` omitted) for v1.0.
5. **Architectural Separation**: Clean Architecture (Service ➔ Domain ➔ Data) paired with Brad Frost's Atomic Design (Atoms ➔ Molecules ➔ Organisms ➔ Theme Templates).

---

### B. Standard Development Commands

```bash
# Build & Compile
./gradlew assembleDebug

# Run Unit & Seam Tests
./gradlew testDebugUnitTest --continue

# Run Android Lint Check
./gradlew lintDebug

# Install Directly to Connected Device / Emulator
./gradlew installDebug

# Release AAB Size & ProGuard Verification
./gradlew bundleRelease
```

---

### C. Project Directory Architecture

```text
app/src/main/java/com/notificationbubble/app/
├── core/                         # Dispatchers, logging, extensions
│   ├── dispatchers/
│   └── util/
├── data/                         # Local Room database & Billing
│   ├── local/                    # AppDatabase, DAOs, Entities
│   └── billing/                  # Google Play Billing v6 repository
├── domain/                       # Pure business models & contracts
│   ├── model/                    # NotificationItem, BubbleState, ThemeTokens
│   └── repository/               # NotificationRepository, SettingsRepository
├── service/                      # Android OS background services
│   ├── NotificationListener.kt   # Ingests OS notifications
│   ├── OverlayService.kt         # Manages WindowManager & Compose host
│   └── BootReceiver.kt           # Auto-starts service upon device boot
└── ui/                           # Atomic Jetpack Compose UI
    ├── theme/                    # Design tokens (Colors, Typography, BubbleTheme)
    ├── atoms/                    # AvatarCircle, AppBadgeIcon, ActionPill, UnreadCounter
    ├── molecules/                # FloatingBubbleItem (with spring gestures), QuickReplyInput
    ├── organisms/                # BubbleStack, ExpandedFlyoutCard, DismissTargetZone
    ├── onboarding/               # MascotWizardScreen, AppPickerList, OEMGuideScreen
    └── settings/                 # SettingsScreen, ThemePickerScreen, ProPaywallScreen
```

---

### D. Code Style & Mental Model (React ➔ Compose Analogy)

All UI components must be modular, side-effect free, and use Unidirectional Data Flow (UDF):

```kotlin
// Example Molecule: FloatingBubbleItem
@Composable
fun FloatingBubbleItem(
    avatarUri: String?,
    appIconRes: Int,
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clickable(onClick = onClick)
    ) {
        // Atom 1: Circular Avatar
        AvatarCircle(uri = avatarUri, modifier = Modifier.fillMaxSize())
        
        // Atom 2: Bottom-right App Badge Icon
        AppBadgeIcon(iconRes = appIconRes, modifier = Modifier.align(Alignment.BottomEnd))
        
        // Atom 3: Top-right Unread Counter Badge
        if (unreadCount > 1) {
            UnreadCounterBadge(count = unreadCount, modifier = Modifier.align(Alignment.TopEnd))
        }
    }
}
```

---

### E. Architectural Boundaries

- **ALWAYS**:
  - Run `./gradlew testDebugUnitTest` before proposing code changes.
  - Enforce `16dp` safe margins from screen borders to protect system gesture navigation.
  - Keep touch loops allocation-free (reuse math objects, no garbage collection in drag).
- **ASK FIRST**:
  - Adding any third-party dependency to `build.gradle.kts`.
  - Modifying Room database entity schemas or version numbers.
- **NEVER**:
  - Add `android.permission.INTERNET` to `AndroidManifest.xml` in v1.0.
  - Put ads inside the floating bubble overlay window.
  - Render full-screen blocking UI over active games or video playback.

---

## 2. Implementable Developer Task Cards

Every task card is atomic, completable in a single focused session, and touches $\le 5$ files.

---

### Sprint 0: Foundation & Overlay Engine (Tasks 01–04)

#### [x] Task 01: Android Project Scaffolding & Lean Gradle Config
- **User Story Reference**: Story 18, 19
- **Files Touched**: `build.gradle.kts`, `app/build.gradle.kts`, `settings.gradle.kts`, `app/src/main/AndroidManifest.xml`
- **Acceptance Criteria**:
  - Root and app-level Gradle kts configured with Kotlin 2.0+ and Jetpack Compose BOM.
  - `minSdk = 29`, `targetSdk = 35`.
  - ProGuard/R8 configured for release (`isMinifyEnabled = true`, `isShrinkResources = true`).
  - Zero internet permissions in manifest.
- **Verification**: Run `./gradlew assembleDebug` &mdash; compiles cleanly with zero warnings.

#### [x] Task 02: Full-Screen Transparent WindowManager Compose Host
- **User Story Reference**: Story 5
- **Files Touched**: `service/OverlayService.kt`, `ui/OverlayHostView.kt`
- **Acceptance Criteria**:
  - Creates a transparent `SYSTEM_ALERT_WINDOW` with `FLAG_NOT_TOUCH_MODAL` and `FLAG_NOT_FOCUSABLE`.
  - Hosts a Jetpack Compose View hierarchy attached to Android WindowManager.
  - Touches outside the bubble pass straight through to underlying apps.
- **Verification**: Launch service; verify background apps receive clicks normally.

#### [x] Task 03: Resilient Foreground Service & Boot Revival
- **User Story Reference**: Story 3
- **Files Touched**: `service/OverlayService.kt`, `service/BootReceiver.kt`, `AndroidManifest.xml`
- **Acceptance Criteria**:
  - `OverlayService` runs as a Foreground Service with a discrete, low-priority ongoing notification.
  - Configures `START_STICKY` so OS restarts service if memory pressure clears.
  - `BootReceiver` listens to `RECEIVE_BOOT_COMPLETED` and re-attaches service on phone restart.
- **Verification**: Force-kill service via `adb shell am kill`; verify it restarts automatically.

#### [x] Task 04: Spring Physics Touch Controller & Screen-Edge Snapping
- **User Story Reference**: Story 6, 7, 8
- **Files Touched**: `ui/interaction/BubblePhysicsController.kt`, `ui/interaction/SpringMotion.kt`
- **Acceptance Criteria**:
  - Implements pointer input drag gestures with pointer velocity tracking.
  - On finger release, snaps to nearest left/right screen edge with damped spring bounce (`StiffnessMediumLow`, `DampingRatioLowBouncy`).
  - Enforces `16dp` inset margin from display edge.
- **Verification**: Drag bubble across screen on emulator; release and observe organic spring recoil to the margin.

---

### Sprint 1: Atomic UI & Drag Interactivity (Tasks 05–08)

#### [x] Task 05: Atomic Visual Primitives (`AvatarCircle`, `AppBadgeIcon`, `UnreadCounterBadge`)
- **User Story Reference**: Story 5, 11
- **Files Touched**: `ui/atoms/AvatarCircle.kt`, `ui/atoms/AppBadgeIcon.kt`, `ui/atoms/UnreadCounterBadge.kt`, `ui/theme/BubbleTheme.kt`
- **Acceptance Criteria**:
  - `AvatarCircle`: 56dp circular cropped image with placeholder fallback.
  - `AppBadgeIcon`: 16dp circular badge anchored at bottom-right.
  - `UnreadCounterBadge`: High-contrast pill badge anchored at top-right for count $> 1$.
- **Verification**: Compose Preview renders all three visual atoms cleanly across light/dark themes.

#### [x] Task 06: Interactive Molecule `FloatingBubbleItem` with Haptic Feedback
- **User Story Reference**: Story 5, 6
- **Files Touched**: `ui/molecules/FloatingBubbleItem.kt`
- **Acceptance Criteria**:
  - Composes avatar, app badge, and counter into a single interactive 56dp unit.
  - Implements press-scale micro-animation (scales down to 0.92f on press, springs back to 1.0f on release).
  - Triggers light haptic feedback on touch down.
- **Verification**: Tap bubble on device; verify subtle press animation and haptic vibration.

#### [ ] Task 07: Organism `DismissTargetZone` ("The Magnetic Black Hole")
- **User Story Reference**: Story 9
- **Files Touched**: `ui/organisms/DismissTargetZone.kt`, `ui/interaction/MagneticPullSensor.kt`
- **Acceptance Criteria**:
  - Circular "X" target animates into view at bottom-center when dragging begins.
  - When bubble is within 100dp radius of target, magnetically pulls bubble toward center.
  - Releasing inside target dismisses bubble with scale-to-zero exit animation.
- **Verification**: Drag bubble toward bottom-center; observe magnetic attraction and smooth dismissal.

#### [x] Task 08: Organism `ActiveBubbleStack` (Multi-Conversation Stagger)
- **User Story Reference**: Story 11
- **Files Touched**: `ui/organisms/ActiveBubbleStack.kt`, `domain/model/BubbleStackState.kt`
- **Acceptance Criteria**:
  - Manages multiple active bubbles staggered with a 12dp horizontal/vertical offset.
  - Tapping stack cycles through active senders or expands the active head.
  - Maximum 3 active heads shown in stack; excess collapsed into unread count.
- **Verification**: Simulate 3 incoming messages; verify clean multi-head stacked arrangement.

---

### Sprint 2: Notification Listener & Filtering (Tasks 09–12)

#### [ ] Task 09: `NotificationListenerService` Ingestion Pipeline
- **User Story Reference**: Story 5
- **Files Touched**: `service/NotificationListener.kt`, `domain/repository/NotificationRepository.kt`
- **Acceptance Criteria**:
  - Extends `NotificationListenerService` to intercept `StatusBarNotification` events.
  - Extracts sender name, avatar bitmap, message text, and app package.
  - Discards ongoing notifications (media players, foreground downloads).
- **Verification**: Send test SMS/notification via ADB; verify listener logs structured data.

#### [ ] Task 10: Local Room SQLite Database for App Whitelist & Settings
- **User Story Reference**: Story 4
- **Files Touched**: `data/local/AppDatabase.kt`, `data/local/entity/AppRuleEntity.kt`, `data/local/dao/AppRuleDao.kt`
- **Acceptance Criteria**:
  - Room database storing per-app whitelist state (`isWhitelisted: Boolean`).
  - Stores persistent user preferences (bubble size, default theme).
  - Pre-populates popular chat app IDs.
- **Verification**: Run Room DAO unit tests verifying insert, query, and toggle operations.

#### [ ] Task 11: Sensitive 2FA / OTP Auto-Masking Filter
- **User Story Reference**: Story 16
- **Files Touched**: `domain/util/SensitiveContentFilter.kt`, `domain/util/SensitiveContentFilterTest.kt`
- **Acceptance Criteria**:
  - Regex detects OTP verification codes, banking alert keywords, and 2FA SMS tokens.
  - Masks preview text to `"•••••• (Tap to view)"` in bubble preview.
  - Unmasks only on explicit intentional tap.
- **Verification**: Run unit test suite testing 20 diverse banking and OTP text patterns.

#### [ ] Task 12: Onboarding App Picker (0 Default + `[Chats Only]` Preset)
- **User Story Reference**: Story 4
- **Files Touched**: `ui/onboarding/AppPickerList.kt`, `ui/onboarding/AppPickerViewModel.kt`
- **Acceptance Criteria**:
  - Queries `PackageManager` for installed launchable apps.
  - Zero apps checked by default.
  - 1-tap `[Chats Only]` preset automatically selects WhatsApp, Telegram, Signal, SMS, Discord.
- **Verification**: Launch App Picker screen; tap `[Chats Only]` and verify checked state.

---

### Sprint 3: Flyout Card, Keyboard & Insets (Tasks 13–16)

#### [ ] Task 13: Molecule `ExpandedFlyoutCard` with Action Pills
- **User Story Reference**: Story 10, 13
- **Files Touched**: `ui/molecules/ExpandedFlyoutCard.kt`, `ui/atoms/ActionPill.kt`
- **Acceptance Criteria**:
  - Floating card appearing adjacent to the active bubble.
  - Shows sender name, 2-line snippet preview, and timestamp.
  - Action pills: `[Reply]`, `[Mark Read]`, and `[Dismiss]`.
- **Verification**: Tap active bubble; preview card expands smoothly with spring enter transition.

#### [ ] Task 14: Keyboard Inset Sensor (`WindowInsets.Type.ime()`)
- **User Story Reference**: Story 14
- **Files Touched**: `ui/interaction/KeyboardInsetObserver.kt`, `service/OverlayService.kt`
- **Acceptance Criteria**:
  - Observes Android on-screen keyboard height changes in real-time.
  - When keyboard opens, shifts active bubble and flyout card upward above keyboard top border.
  - Resets coordinates when keyboard closes.
- **Verification**: Open Gboard in underlying app; verify bubble slides up without blocking keys.

#### [ ] Task 15: Landscape & Full-Screen Auto-Dock to Side-Pill
- **User Story Reference**: Story 15
- **Files Touched**: `ui/interaction/OrientationObserver.kt`, `ui/atoms/DockedSidePill.kt`
- **Acceptance Criteria**:
  - Detects orientation change to landscape or full-screen gaming mode.
  - Automatically collapses bubble into a 40% opacity edge tab.
  - Tapping side-tab restores normal bubble state.
- **Verification**: Rotate phone to landscape; verify bubble transitions to unobtrusive side-pill.

#### [ ] Task 16: Inline Quick Reply Intent & RemoteInput Dispatch
- **User Story Reference**: Story 12
- **Files Touched**: `ui/molecules/QuickReplyInput.kt`, `service/NotificationActionDispatcher.kt`
- **Acceptance Criteria**:
  - Tapping `[Reply]` expands an inline text input field with Send icon.
  - Submitting message extracts `Notification.Action` with `RemoteInput` and dispatches reply directly to Android OS.
  - Clears bubble preview upon successful dispatch.
- **Verification**: Send reply from bubble to test WhatsApp message; verify sent on recipient device.

---

### Sprint 4: Onboarding, Monetization & Release (Tasks 17–20)

#### [ ] Task 17: Mascot Onboarding Flow ("Bubby / Pebble" Permission Wizard)
- **User Story Reference**: Story 1, 2
- **Files Touched**: `ui/onboarding/MascotWizardScreen.kt`, `ui/onboarding/MascotIllustration.kt`
- **Acceptance Criteria**:
  - Multi-step onboarding featuring the mascot character.
  - Guides user through Overlay (`ACTION_MANAGE_OVERLAY_PERMISSION`) and Notification Listener setup.
  - Prominent "100% On-Device / Zero-Network Guaranteed" privacy badge.
- **Verification**: Walk through onboarding on a fresh install; verify all permissions are granted.

#### [ ] Task 18: OEM Battery Exemption Deep-Link Wizard (`dontkillmyapp`)
- **User Story Reference**: Story 3
- **Files Touched**: `service/OEMExemptionHelper.kt`, `ui/onboarding/OEMSetupDialog.kt`
- **Acceptance Criteria**:
  - Detects device manufacturer (`Build.MANUFACTURER`: Samsung, Xiaomi, OnePlus, Huawei).
  - Provides direct 1-tap intent to phone-specific autostart and battery exemption settings.
- **Verification**: Test on Samsung/Xiaomi device; tap button and verify correct system settings page opens.

#### [ ] Task 19: Google Play Billing v6 Integration ($3.99 Lifetime Pro)
- **User Story Reference**: Story 19, 20
- **Files Touched**: `data/billing/BillingRepository.kt`, `ui/settings/ProPaywallScreen.kt`
- **Acceptance Criteria**:
  - Implements `BillingClient` v6+ for a single non-consumable in-app purchase (`pro_lifetime_unlock`).
  - Gates unlimited apps, OLED Dark, and Frosted Glass themes behind Pro status.
  - 1-tap `[Restore Purchase]` queries active purchases and persists state in Room.
- **Verification**: Perform test purchase with Google Play license test account; verify Pro features unlock.

#### [ ] Task 20: R8 ProGuard Optimization, Vector Assets & < 5MB Binary Audit
- **User Story Reference**: Architecture Contract
- **Files Touched**: `app/proguard-rules.pro`, `app/build.gradle.kts`
- **Acceptance Criteria**:
  - All UI icons verified as XML Vector Drawables (~1KB each).
  - R8 full obfuscation and resource shrinking enabled.
  - Release AAB generated via `./gradlew bundleRelease`.
- **Verification**: Inspect generated `.aab` file size &mdash; must be strictly **$< 5.0\text{ MB}$**.

---

## 3. Progress Tracking & Next Step

Once committed, these 20 tasks will be tracked and checked off sequentially during implementation:
- **Sprint 0** (Tasks 01–04): Foundation & Window Engine
- **Sprint 1** (Tasks 05–08): Atomic UI & Drag Interactivity
- **Sprint 2** (Tasks 09–12): Notification Ingestion & Filtering
- **Sprint 3** (Tasks 13–16): Flyout Card, Keyboard & Insets
- **Sprint 4** (Tasks 17–20): Onboarding, Monetization & Release
