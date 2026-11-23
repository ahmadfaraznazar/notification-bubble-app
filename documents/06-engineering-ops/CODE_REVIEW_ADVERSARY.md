# Phase 6.2: Adversarial Code Review Checklist & Multi-Axis Quality Audit

> **Module**: Notification Bubble App Adversarial Engineering Review  
> **Status**: APPROVED & COMPLETE  
> **Audit Framework**: 5-Axis Code Review Protocol (Correctness, Security, Readability, Performance, Tests)  
> **Target Environment**: Android 10+ (API 29–35), Jetpack Compose, WindowManager Overlay, Room SQLite  
> **Benchmarked Standards**: Chromium Android Review Guidelines, Signal-Android Security Protocol  

---

## 1. Executive Summary & Review Philosophy

A code review for a background system overlay is not a stylistic debate—it is an **adversarial defense against real-world device failures**. 

Floating overlays operate under unforgiving constraints:
- An unhandled exception in an overlay crashes above other apps, destroying user trust.
- A single object allocation in a drag touch loop drops frames on 120Hz displays.
- A leaked `Context` or `Bitmap` holds 50MB in memory until aggressive OEM task managers terminate the app.
- An accidental network dependency violates our core "Zero-Network Privacy" promise.

Every Pull Request must be reviewed through the lens of one guiding question:
> *"Will this code cause a memory leak, crash an active full-screen game, drop frames on 120Hz displays, or leak private notification data within three months?"*

```
┌─────────────────────────────────────────────────────────────────┐
│                    THE 5-AXIS AUDIT HIERARCHY                   │
├───────────────────────┬─────────────────────────────────────────┤
│ 1. CORRECTNESS        │ Edge cases, race conditions, nulls, OS  │
│    (BLOCKING)         │ WindowManager token invalidations       │
├───────────────────────┼─────────────────────────────────────────┤
│ 2. SECURITY & PRIVACY │ Zero-network manifest checks, logcat    │
│    (BLOCKING)         │ redaction, regex OTP masking leaks      │
├───────────────────────┼─────────────────────────────────────────┤
│ 3. PERFORMANCE & MEM  │ Zero heap allocations in 120Hz drag,    │
│    (BLOCKING)         │ Compose stability, bitmap recycling     │
├───────────────────────┼─────────────────────────────────────────┤
│ 4. READABILITY        │ Clean Architecture domain boundaries,   │
│    (NON-BLOCKING)     │ naming conventions, magic numbers       │
├───────────────────────┼─────────────────────────────────────────┤
│ 5. TEST COVERAGE      │ Concurrency stress tests, notification  │
│    (BLOCKING)         │ storm handling, Robolectric DAOs        │
└───────────────────────┴─────────────────────────────────────────┘
```

---

## 2. Axis 1: Correctness & Concurrency Traps

### 1.1 WindowManager Overlay Token Invalidation
* **The Failure**: Calling `windowManager.updateViewLayout()` or `windowManager.removeView()` after the hosting `Service` has been destroyed, or when the view has not yet been attached.
* **The Check**: Verify every WindowManager operation is wrapped in a lifecycle-safe check and guarded against `IllegalArgumentException` (`"View not attached to window manager"`):
  ```kotlin
  // ❌ MUST FIX: Crash if service is destroyed during animation
  windowManager.removeView(overlayView)

  // ✅ CORRECT: Safe defensive removal
  if (overlayView.isAttachedToWindow) {
      runCatching { windowManager.removeView(overlayView) }
          .onFailure { BubbleLog.w("Overlay", "Safe view removal failed", it) }
  }
  ```

### 1.2 Notification Event Race Conditions
* **The Failure**: Intercepting `onNotificationPosted()` and `onNotificationRemoved()` concurrently across multiple threads for the same key, causing ghost bubbles or stale unread counters.
* **The Check**: Verify all notification state mutations pass through a single serialized queue or atomic state holder (e.g. `MutableStateFlow.update { ... }` or an actor-style mutex).

### 1.3 Null / Malformed Notification Extras
* **The Failure**: Assuming `sbn.notification.extras.getString(Notification.EXTRA_TITLE)` is never null. Messaging apps frequently deliver empty titles, SpannableStrings, or null bitmaps for group chats.
* **The Check**:
  - Safe unwrap with fallbacks: `extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "New Message"`.
  - Handle oversized titles ($> 200$ chars) by clamping before domain ingestion.

### 1.4 Touch Event Cancellation (`ACTION_CANCEL`)
* **The Failure**: When an incoming call or system dialog overlays the screen, Android dispatches `MotionEvent.ACTION_CANCEL`. If the touch loop only listens for `ACTION_UP`, the bubble gets permanently stuck in dragging scale (`0.92f`) and fails to snap to an edge.
* **The Check**: Verify gesture recognizers explicitly treat `ACTION_CANCEL` as an automatic reset and snap to the nearest margin.

---

## 3. Axis 2: Security & Privacy Traps

### 2.1 Zero-Network Manifest Enforcement
* **The Failure**: A PR introduces a convenience library (e.g. an image cropper or analytics tool) that pulls in `android.permission.INTERNET` via Gradle manifest merger.
* **The Check**:
  - Inspect merged manifest via `./gradlew processDebugManifest`.
  - Verify script `scripts/verify-zero-network.sh` runs and exits with code 0.
  - Reject any PR adding `okhttp`, `retrofit`, `ktor`, or analytics dependencies.

### 2.2 Redacted Logcat Audit
* **The Failure**: Developer inserts `Log.d(TAG, "Message received: $text")` to debug a notification parsing issue and forgets to remove it.
* **The Check**:
  - Grep diff for `Log.d`, `Log.v`, `println`, `System.out`.
  - Ensure all logging routes strictly through `BubbleLog`.
  - Verify that no message body, sender name, or contact number is ever passed to logging parameters.

### 2.3 Sensitive 2FA / OTP Auto-Masking Bypass
* **The Failure**: A new regex pattern for banking alerts fails to catch mixed-case strings (`"Otp: 123456"` or `"Your verification code is 987654"`).
* **The Check**:
  - Verify regex pattern case-insensitivity: `Regex("(?i)\\b(\\d{4,8}|[A-Z0-9]{6})\\b")`.
  - Adversarial test cases must cover SMS, WhatsApp, and Telegram banking alert structures.

---

## 4. Axis 3: Performance, Frame-Drops & Memory Leaks

### 4.1 Zero Heap Allocation in Drag Loops (120Hz Law)
* **The Failure**: Creating new instances of `Offset`, `Rect`, or lambda closures inside pointer drag callbacks causes frequent Garbage Collection (GC) pauses, dropping frames below 120Hz.
* **The Check**:
  ```kotlin
  // ❌ MUST FIX: Allocates new Offset on every finger micro-move (60-120 times/sec)
  Modifier.pointerInput(Unit) {
      detectDragGestures { change, dragAmount ->
          controller.updateOffset(Offset(dragAmount.x, dragAmount.y))
      }
  }

  // ✅ CORRECT: Reuses mutable primitive coordinates
  controller.updateDragDelta(dragAmount.x, dragAmount.y)
  ```

### 4.2 Context Leak in Overlay Singletons
* **The Failure**: Storing an `Activity` context inside `BubblePhysicsController`, `BillingManager`, or custom views.
* **The Check**: Overlays must strictly use `applicationContext` or the `Service` context. Never hold references to transient Activities.

### 4.3 Compose Stability & Recomposition Scopes
* **The Failure**: Passing standard standard Kotlin `List<NotificationMessage>` into `ActiveBubbleStack`. Because standard `List` is an interface with non-guaranteed immutability, Compose marks it as *unstable* and recomposes the entire stack on every frame during dragging.
* **The Check**:
  - Enforce `ImmutableList<T>` from `kotlinx.collections.immutable` or mark domain models with `@Immutable` / `@Stable`.
  - Verify layout passes using the Compose Layout Inspector.

### 4.4 Large Bitmap Memory Bloat
* **The Failure**: Storing raw unscaled contact avatars ($1024 \times 1024$ Bitmaps) in memory for multiple active bubbles, consuming $4\text{MB}$ per head.
* **The Check**:
  - Avatars must be downsampled immediately upon ingestion to $112 \times 112\text{px}$ (exact $2 \times$ density for $56\text{dp}$).
  - Raw bitmaps must be recycled or managed via Coil's bounded memory cache.

---

## 5. Axis 4: Readability & Architectural Boundaries

### 5.1 Clean Architecture Boundary Enforcement
* **The Rule**: The `domain/` module represents pure business rules.
* **The Check**:
  - Run: `grep -rn "import android\." app/src/main/java/com/bubble/domain/`
  - Any match (e.g. `android.graphics.Bitmap`, `android.os.Bundle`, `android.content.Context`) is an **automatic PR block**.
  - Pass byte arrays (`ByteArray`) or domain wrapper models (`AvatarReference`) instead of Android OS classes.

### 5.2 Elimination of Magic Numbers
* **The Rule**: All spatial values, durations, and spring constants must reference `BubbleDimensions`, `BubbleElevation`, or `BubbleSpacing`.
* **The Check**: Reject hardcoded numeric dp/sp literals (e.g. `Modifier.padding(14.dp)` instead of `BubbleSpacing.Spacing16`).

---

## 6. Axis 5: Test Coverage & Edge Hunting

A PR cannot merge based purely on passing unit tests—the tests must aggressively explore failure modes:

| Test Target | Mandatory Adversarial Test Cases | Pass Condition |
|---|---|---|
| **`BubblePhysicsController`** | • Dragging past screen bounds ($X < 0$, $X > \text{Width}$)<br>• Zero-velocity release<br>• High-velocity fling ($V_x = 3000\text{dp/s}$)<br>• Fling directly into screen corners | Bubble coordinates remain strictly clamped within $16\text{dp}$ safe margins |
| **`AppRuleDao`** | • Querying uninstalled app package<br>• Toggling 4th app when `isProUnlocked = false`<br>• Database corruption recovery | Sub-millisecond lookup; rejects 4th app insertion cleanly |
| **`SensitiveContentFilter`** | • 4-digit, 6-digit, 8-digit OTPs<br>• Alphanumeric codes (`G-123456`)<br>• Banking debit alerts with dollar/euro symbols<br>• False-positive test (regular text containing digits) | Redacts genuine sensitive tokens; does not mask harmless time strings ("Meet at 5pm") |
| **Notification Storm** | • Injecting 50 rapid `StatusBarNotification` events in $500\text{ms}$ | Zero dropped events; UI throttles animations cleanly without dropping frames |

---

## 7. Actionable PR Review Comment Templates

Reviewers must use the standard triage syntax:
`[Severity][Axis] file:line problem → concrete fix`

### Examples of Enforced Comments

```text
[Must Fix][Security] NotificationListener.kt:74
Logging raw intent extras exposes user SMS preview to system logcat.
→ Replace `Log.d(TAG, extras.toString())` with `BubbleLog.logNotificationReceived(sbn.packageName, body.length, isMasked)`.

[Must Fix][Performance] FloatingBubbleItem.kt:112
Allocating a new SpringSpec object on every pointer drag event causes GC stutter on 120Hz displays.
→ Hoist `val snapSpec = remember { spring<Offset>(...) }` outside the pointer input block.

[Must Fix][Correctness] OverlayService.kt:185
Calling `windowManager.updateViewLayout()` without verifying `isAttachedToWindow` crashes if called during service shutdown.
→ Wrap in `if (overlayView.isAttachedToWindow) { ... }`.

[Should Fix][Readability] DismissTargetZone.kt:45
Magic number `88.dp` used for expanded dismiss target size.
→ Replace with `BubbleDimensions.DismissTargetActiveSize`.
```

---

## 8. Verification & Quality Checklist

- [x] 5-Axis review protocol strictly codified with clear blocking vs. non-blocking criteria.
- [x] Specific failure modes defined for Android `WindowManager` overlay lifecycles.
- [x] Zero-allocation touch loop rules specified for 120Hz frame preservation.
- [x] Zero-network manifest audit and redacted logcat checks mandated.
- [x] Concurrency and notification storm stress testing rules established.
- [x] Standard actionable PR review comment templates provided.
