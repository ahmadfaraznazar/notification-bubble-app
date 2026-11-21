# Android Best Practices & Lifecycle Specification: Notification Bubble App

> **Objective**: Platform engineering standards, Android 14/15 lifecycle compliance, zero-allocation 120Hz gesture loops, and battery survival laws for the Notification Bubble App.  
> **Skill Activated**: `android-best-practices` ([SKILL.md](file:///Users/ahmadnazar/Documents/GitHub/notification-bubble-app/ai-skills/01-app-development/03-architecture-and-tech/android-best-practices/SKILL.md))  
> **Date**: 2026-11-22  

---

## 1. Executive Summary & Core Engineering Laws

To deliver a production-grade Android utility that survives modern OS battery killers and renders fluid 120Hz spring physics, this specification establishes three non-negotiable platform laws:

1. **The 8.3ms Frame Budget (120Hz Compliance)**:
   * Modern OLED screens refresh every **8.3ms** (120Hz).
   * Zero objects may be instantiated inside touch move callbacks (`onDrag`, `pointerInput`). Garbage collection pauses during gesture tracking drop frames and cause noticeable drag stutter.
2. **Foreground Service Special Use (Android 14/15 Compliance)**:
   * Android 14+ strictly terminates foreground services that lack a declared type.
   * `OverlayService` must declare `android:foregroundServiceType="specialUse"` with compliant Play Store metadata.
3. **Strict State Immutability & Compose Stability**:
   * All UI state models must be verified as `@Immutable` by the Compose Compiler to skip unnecessary recompositions of the overlay canvas.

---

## 2. WindowManager & Overlay Engine Architecture

### A. WindowManager Layout Parameters
The floating overlay window must be configured with precise system flags to allow touches to pass through outside the active bubble:

```kotlin
val overlayParams = WindowManager.LayoutParams(
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
    // Enable software cutout avoidance
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
    }
}
```

* **Why `FLAG_NOT_TOUCH_MODAL`**: Ensures that touch events occurring outside the bubble hit-box pass directly through to whatever game, browser, or app is running beneath.
* **Why `FLAG_NOT_FOCUSABLE`**: Prevents the overlay from stealing keyboard focus from underlying apps until the user explicitly taps `[Reply]`.

---

## 3. Zero-Allocation 120Hz Gesture Tracking

### Touch Loop Memory Optimization
In Jetpack Compose, dragging a bubble across the screen triggers up to 120 touch events per second:

```kotlin
// BAD (Allocates 120 new Point/Offset objects per second, triggering GC pauses):
Modifier.pointerInput(Unit) {
    detectDragGestures { change, dragAmount ->
        bubbleOffset = Offset(bubbleOffset.x + dragAmount.x, bubbleOffset.y + dragAmount.y)
    }
}

// GOOD (Reuses existing Animatable vector channels with zero object churn):
val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

Modifier.pointerInput(Unit) {
    detectDragGestures(
        onDrag = { change, dragAmount ->
            change.consume()
            // In-place channel update on the rendering thread
            coroutineScope.launch {
                animatedOffset.snapTo(
                    Offset(
                        x = (animatedOffset.value.x + dragAmount.x).coerceIn(minX, maxX),
                        y = (animatedOffset.value.y + dragAmount.y).coerceIn(minY, maxY)
                    )
                )
            }
        },
        onDragEnd = {
            // Damped spring snap on finger lift
            coroutineScope.launch {
                val targetX = if (animatedOffset.value.x < screenMidPoint) edgeMargin else screenWidth - edgeMargin
                animatedOffset.animateTo(
                    targetValue = Offset(targetX, animatedOffset.value.y),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    )
}
```

---

## 4. Background Service Resilience (Surviving Android 14/15 Doze)

### A. Manifest Service Declaration
```xml
<service
    android:name=".service.OverlayService"
    android:exported="false"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="Floating conversation head overlay for user-selected messaging applications" />
</service>

<service
    android:name=".service.NotificationListener"
    android:exported="true"
    android:label="@string/app_name"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```

### B. Persistent Foreground Notification Channel
* **Importance**: `NotificationManager.IMPORTANCE_MIN` (Silent & Low Priority).
* **Visibility**: Shows a discrete status message: *"Notification Bubble Service is running"*.
* **Ongoing**: `setOngoing(true)` so user swipes cannot dismiss the background anchor.
* **Return Value**: `onStartCommand` returns `START_STICKY`, instructing Android OS to recreate the service if killed during high RAM pressure.

---

## 5. Compose State Management & Recomposition Stability

To guarantee that only the affected UI components re-render:

1. **State Inversion**: Never pass `ViewModel` instances into Atoms or Molecules. Pass only primitive state and lambda callbacks:
   ```kotlin
   // GOOD:
   @Composable
   fun FloatingBubbleItem(
       state: BubbleUiModel,
       onBubbleClick: (String) -> Unit,
       onDismiss: (String) -> Unit
   )
   ```
2. **`@Immutable` Annotation on Models**: Every domain and UI data model must be annotated with `@Immutable` so the Compose compiler knows properties never mutate unexpectedly.
3. **`derivedStateOf` for Inset Calculations**:
   ```kotlin
   val isNearDismissTarget by remember {
       derivedStateOf {
           calculateDistance(bubbleOffset.value, dismissTargetCenter) < 100.dp
       }
   }
   ```
   This ensures Compose only recomposes when the boolean result changes, rather than recalculating during every pixel of drag movement.

---

## 6. Actionable Takeaways for Step 3.4

- In **Step 3.4 (`api-and-data-modeling`)**, we design the Room SQLite database schema and entity mappers to store app whitelists, conversation queues, and purchase tokens offline.
