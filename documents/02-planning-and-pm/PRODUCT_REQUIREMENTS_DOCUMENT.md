# Product Requirements Document (PRD): Notification Bubble App (MVP v1.0)

> **Document Type**: MVP Product Requirements Document & Scope Specification  
> **Skill Activated**: `prd-and-scope-cutter` ([SKILL.md](file:///Users/ahmadnazar/Documents/GitHub/notification-bubble-app/ai-skills/01-app-development/02-planning-and-pm/prd-and-scope-cutter/SKILL.md))  
> **Status**: Ready for Agent Implementation (`ready-for-agent`)  
> **Date**: 2026-11-21  

---

## 1. Problem Statement

Modern smartphone notification management creates significant daily friction for power users and multitaskers:
1. **The Cluttered Notification Shade**: The pull-down notification panel on Android is noisy, cluttered with marketing spam, download progress bars, and low-priority alerts. Reading a new message requires pulling down a heavy shade that completely obstructs the active application.
2. **Loss of Beloved Chat Heads**: Users loved the fluid, tactile Facebook Messenger Chat Heads paradigm. However, Meta buried them inside complex OS opt-ins, and other primary messaging apps (WhatsApp, Telegram, Signal) have no native chat head support.
3. **Broken Third-Party Alternatives**: Existing alternatives on Google Play (*DirectChat*, *Flychat*) are either abandoned/delisted or plagued by full-screen video ads popping up while typing, background kills on Samsung/Xiaomi devices, and intrusive permission requests.

---

## 2. Solution

The **Notification Bubble App** is an ultra-lightweight (< 5MB), ad-free, 100% on-device Android application that brings classic, silky-smooth chat head bubbles to **any user-selected messaging app**:
- **Tactile 120Hz Spring Physics**: Floating circular avatars that snap organically to screen edges with magnetic dismiss targets.
- **Progressive Disclosure**: A 4-state overlay machine that expands into a compact 2-line preview and inline quick reply without opening the full chat app.
- **Zero-Network Privacy**: 100% local processing with zero internet permissions, guaranteeing user messages and OTP codes never leave the device.
- **Rock-Solid Background Stability**: A resilient foreground service and OEM battery-optimization onboarding wizard to prevent process kills on Samsung One UI and Xiaomi HyperOS.

---

## 3. User Stories

### A. Onboarding & Permissions
1. As a new user, I want an interactive onboarding wizard guided by the "Bubby/Pebble" mascot, so that granting overlay and notification permissions feels safe and friendly.
2. As a privacy-conscious user, I want clear reassurance that the app has zero network permissions, so that I know my private chats and banking alerts are safe.
3. As a Samsung or Xiaomi user, I want 1-tap deep links to my phone's battery settings during onboarding, so that the background bubble service is not killed by the OS.
4. As a new user, I want 0 apps selected by default with 1-tap presets (`[Chats Only]`, `[Social Media]`), so that my screen is not bombarded by an avalanche of bubbles on day one.

### B. Core Floating Bubble Mechanics
5. As an active phone user, I want incoming messages from approved apps to spawn a floating 56dp circular bubble, so that I can see who messaged me without interrupting my current task.
6. As a multitasker, I want to drag the bubble freely across the screen with fluid spring physics, so that I can reposition it away from buttons or text I am reading.
7. As a user, I want the bubble to snap to the nearest left or right screen edge with a slight organic bounce when released, so that it stays out of my way.
8. As a user, I want bubbles to maintain a strict `16dp` margin from screen edges, so that my Android edge back-swipes never conflict with the bubble.
9. As a user, I want to drag a bubble down to a bottom-center magnetic target to dismiss it with haptic feedback, so that closing conversations is effortless and satisfying.

### C. Reading & Quick Interactions
10. As a user, I want to tap a bubble to expand a compact, floating 2-line preview card, so that I can read the message without opening the full messaging app.
11. As a user, I want multiple active conversations to stack into staggered heads by sender, so that I can switch between conversations with a single tap.
12. As a user, I want to tap `[Reply]` inside the preview to reveal an inline text field, so that I can respond directly from the overlay.
13. As a user, I want to tap `[Mark as Read]` or `[Dismiss]` on the preview card, so that the notification is cleared from the Android status tray automatically.

### D. OS Context & Distraction Prevention
14. As a user typing in any app, I want floating bubbles to automatically slide up above the on-screen keyboard (`IME`), so that my keys and Send button are never blocked.
15. As a mobile gamer or video viewer, I want bubbles to automatically collapse into an ultra-thin, low-opacity side-pill when in landscape orientation, so that game controls and subtitles are unobstructed.
16. As a user with sensitive notifications, I want 2FA verification codes and banking messages to show masked text (`"••••••"`), so that people near me cannot shoulder-surf private codes.
17. As a user locking my phone, I want active bubbles to automatically dismiss/hide when the screen turns off, so that my conversations remain private.

### E. Customization & Pro Upgrade
18. As a user, I want clean Material 3 (Dynamic Color) styling for free, so that the app matches my phone wallpaper palette out of the box.
19. As a power user, I want to unlock unlimited simultaneous app bubbles and premium theme packs (OLED Black, Frosted Glass) via a single $3.99 lifetime purchase, so that I can customize my experience without recurring subscriptions.
20. As a paid user reinstalling the app, I want a 1-tap `[Restore Purchase]` button, so that my Pro license is instantly restored offline.

---

## 4. Implementation Decisions

### Architectural Decisions
* **Framework**: Pure Native Kotlin + Jetpack Compose.
* **Window Hierarchy**: Single hardware-accelerated full-screen transparent `SYSTEM_ALERT_WINDOW` hosting a Compose Canvas for 120fps GPU transforms.
* **Lifecycle Architecture**: `NotificationListenerService` paired with an ongoing low-priority `ForegroundService` (`START_STICKY`) to ensure 99%+ background uptime across aggressive OEM task managers.
* **Database & Persistence**: Local Room SQLite database to store user app whitelist preferences, conversation queues, and purchase tokens.
* **Network Capability**: Explicitly **omitted** (`android.permission.INTERNET` removed from `AndroidManifest.xml`).
* **Binary Size Constraints**: Strict < 5MB–6MB APK size enforced through R8 shrinking (`isMinifyEnabled = true`, `isShrinkResources = true`) and XML Vector Drawables for all UI assets.

### The 4-State Overlay Machine
```
  [ State 1: Docked Side-Pill ] ──(Notification Arrives)──► [ State 2: Active Bubble ]
                                                                      │
  [ State 4: Expanded Mini-Card ] ◄────(User Taps Bubble)──── [ State 3: Stacked Heads ]
```

---

## 5. Testing Decisions & Seams

To ensure high reliability without brittle tests, testing will focus strictly on **external behavior across 4 primary seams**:

### Seam 1: Notification Ingestion Pipeline
* **Target**: `NotificationProcessingPipeline`
* **Behavior Tested**: Ingest simulated `StatusBarNotification` payloads; verify that non-whitelisted apps are ignored, silent/ongoing notifications (music, downloads) are dropped, and OTP codes are masked correctly.

### Seam 2: Gesture & Physics Engine
* **Target**: `BubblePhysicsController`
* **Behavior Tested**: Given touch drag inputs, verify boundary clamping, velocity fling calculation, left/right edge snap coordinates (with `16dp` safe margin), and magnetic dismiss zone attraction radius (100dp).

### Seam 3: Window Inset Adaptation
* **Target**: `WindowInsetObserver`
* **Behavior Tested**: Simulate keyboard height changes via `WindowInsets.Type.ime()`; verify that bubble coordinates recalculate to rest strictly above the keyboard boundary.

### Seam 4: Purchase Verification & State
* **Target**: `BillingRepository`
* **Behavior Tested**: Verify local Room caching of Google Play billing tokens and correct gating of Pro features (limiting free tier to 3 active apps, unlocking unlimited on Pro).

---

## 6. Out of Scope (Ruthless Non-Goals for MVP v1.0)

To guarantee rapid development and prevent feature bloat, the following items are strictly **OUT OF SCOPE** for v1.0:
1. ❌ **No Cloud Sync / Remote Accounts**: No user login, no server sync, no Firebase auth.
2. ❌ **No AI Smart Replies**: No local LLM or cloud auto-reply generation.
3. ❌ **No In-Bubble Media Streaming**: No playing YouTube videos or voice notes directly inside the bubble preview.
4. ❌ **No Recurring Subscriptions**: No monthly or annual recurring SaaS billing.
5. ❌ **No More Than 3 Themes at Launch**: V1 ships with Material 3 (Free), Dark OLED (Pro), and Frosted Glass (Pro) only. Additional themes will come in v1.1.
6. ❌ **No Ads in Active Chat Overlays**: Zero ads will ever be rendered in the floating bubble.

---

## 7. Further Notes

* **Triage Label**: `ready-for-agent`
* **Dependencies Required**:
  - `androidx.compose.ui:ui` (BOM pinned)
  - `androidx.room:room-runtime` + `room-ktx`
  - `com.android.billingclient:billing-ktx:6.1.0+`
  - `io.coil-kt:coil-compose:2.5.0` (lightweight image loading)
