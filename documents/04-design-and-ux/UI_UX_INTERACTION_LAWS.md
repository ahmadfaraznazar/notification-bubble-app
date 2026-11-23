# Phase 4.2: UI/UX Interaction Laws & Complete State Machine

> **Module**: Notification Bubble App Interaction Architecture  
> **Status**: APPROVED & COMPLETE  
> **Target Framework**: Jetpack Compose 1.7+, Android WindowManager, Foundation Interaction  
> **Benchmarked Standards**: Facebook Messenger Chat Heads, Android Native System Bubbles (AOSP), DynamicSpot (Jawomo), UXKIN UI States Spec  

---

## 1. Executive Summary & Interaction Philosophy

A floating overlay lives in hostile interaction territory: it hovers above full-screen games, video players, virtual keyboards, and Android's native system gesture boundaries. If a floating bubble occludes keystrokes, triggers accidental back-gestures, or drops frames during touch tracking, users uninstall immediately.

This specification codifies the **5 Immutable Interaction Laws** governing bubble movement, avoidance, and spatial ergonomics, alongside a complete **11-state UI/UX state machine** covering all edge cases.

```
┌─────────────────────────────────────────────────────────────────┐
│                 5 IMMUTABLE INTERACTION LAWS                    │
├─────────────────┬─────────────────┬──────────────┬──────────────┤
│ 1. 16dp Gesture │ 2. IME Keyboard │ 3. Landscape │ 4. Magnetic  │
│    Safe Margin  │    Avoidance    │    Side-Pill │    Dismiss   │
│ Never trigger   │ Lift bubble     │ Dock & fade  │ 100dp pull   │
│ OS back-swipe   │ above soft keys │ during media │ into black   │
│ accidentally    │ automatically   │ & gaming     │ hole target  │
├─────────────────┴─────────────────┴──────────────┴──────────────┤
│ 5. 120Hz Zero-Allocation Spring Velocity Snapping              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. The 5 Immutable Interaction Laws

### Law 1: Gesture Navigation Safe Margin ($16\text{dp}$)

* **The Problem**: On Android 10+ gesture navigation, swiping from either the left or right screen edge triggers a system `Back` navigation. If a bubble sits flush against the edge ($0\text{dp}$), dragging it often triggers an unintended back-swipe, popping the underlying app.
* **The Rule**: The bubble's resting center must never approach closer than $16\text{dp} + \text{Radius} = 44\text{dp}$ from the physical screen bezel.
* **Formula**:
  $$\text{Resting } X_{\text{left}} = 16\text{dp}, \quad \text{Resting } X_{\text{right}} = \text{ScreenWidth} - 16\text{dp} - 56\text{dp}$$
* **Vertical Boundary Clamping**: To prevent collision with the notification status bar or system navigation pill, vertical travel is strictly bounded:
  $$Y_{\min} = \text{StatusBarHeight} + 16\text{dp}, \quad Y_{\max} = \text{ScreenHeight} - \text{NavBarHeight} - 56\text{dp} - 16\text{dp}$$

---

### Law 2: Virtual Keyboard Avoidance (`WindowInsets.ime()`)

* **The Problem**: When the user taps a text input in an underlying app (or taps "Reply" in the bubble), the software keyboard (`IME`) animates upward ($260\text{–}340\text{dp}$). Bubbles resting in the lower half of the screen will obscure backspace, enter, or suggestion strips.
* **The Rule**: The overlay window listens directly to `WindowInsets.Type.ime()`. When `ime.isVisible == true`, any bubble currently resting below the keyboard top threshold is smoothly translated upward via an interruptible spring animation.
* **Behavior Spec**:
  ```kotlin
  val keyboardTop = screenHeight - imeInsets.getBottom()
  if (bubbleY + bubbleDiameter > keyboardTop - 16.dp) {
      val targetY = keyboardTop - bubbleDiameter - 16.dp
      animateSpringTo(targetY)
  }
  ```
* **Keyboard Dismissal**: When the keyboard closes, the bubble returns to its remembered user-placed $Y$ coordinate with subtle spring damping.

---

### Law 3: Landscape & Media/Gaming Auto-Collapse (Side-Pill)

* **The Problem**: Full-screen video streaming (YouTube, Netflix) and landscape mobile games (PUBG, Genshin Impact) require unimpeded screen real estate. A $56\text{dp}$ floating head covering game mini-maps or subtitles generates severe friction.
* **The Rule**: When `Configuration.ORIENTATION_LANDSCAPE` is detected or when an app in the exclusion category is running foreground:
  1. The bubble smoothly contracts from a $56\text{dp}$ circular head into an ultra-thin docked **Side-Pill** ($12\text{dp} \text{ wide} \times 36\text{dp} \text{ high}$).
  2. Opacity reduces from $1.0f$ to $0.4f$ (translucent ambient state).
  3. Badge counters collapse into a single $4\text{dp}$ colored dot.
  4. Touching the side-pill expands it back to the full $56\text{dp}$ head for $5\text{ seconds}$ before auto-re-docking.

---

### Law 4: Magnetic Drag-to-Dismiss ("The Black Hole")

* **The Problem**: In legacy overlay apps (e.g. DirectChat), dismissing requires aiming a bubble precisely onto a tiny static "X" icon. High-speed flings frequently miss, frustrating users.
* **The Rule**: A two-stage gravitational magnetic target zone:
  1. **Stage 1 (Touch Begin)**: As soon as pointer drag exceeds $8\text{dp}$ slop, the bottom Dismiss Target Zone ($64\text{dp}$ circle) fades in at bottom center with a spring scale from $0.5f \to 1.0f$.
  2. **Stage 2 (Gravitational Capture)**: When the distance between the bubble center $(X_b, Y_b)$ and target center $(X_t, Y_t)$ is $\le 100\text{dp}$:
     - The target circle scales up from $64\text{dp} \to 88\text{dp}$ and morphs from neutral dark grey to urgent crimson (`#D32F2F`).
     - A heavy haptic tick (`HapticFeedbackType.LongPress` / `VibrationEffect.createPredefined(EFFECT_TICK)`) fires once upon entry.
     - The bubble's coordinates are pulled toward $(X_t, Y_t)$ using a non-linear magnetic attraction formula:
       $$P_{\text{pulled}} = P_{\text{touch}} + (P_{\text{target}} - P_{\text{touch}}) \times \left(1.0 - \frac{d}{100\text{dp}}\right)^{1.5}$$
  3. **Stage 3 (Release in Target)**: If finger releases while captured, bubble scales down to $0.0f$ in $120\text{ms}$ and active notification is dismissed. Target disappears with $150\text{ms}$ alpha fade.
  4. **Stage 4 (Escape)**: If the user drags away past $100\text{dp}$, target scales back to $64\text{dp}$, crimson tint returns to dark grey, and bubble regains $1:1$ finger tracking.

---

### Law 5: 120Hz Zero-Allocation Spring Velocity Snapping

* **The Problem**: Linear animations feel robotic and unresponsive. In contrast, unconstrained physics that allocate new objects in `onPointerEvent` cause Garbage Collection (GC) pauses and dropped frames on 120Hz displays.
* **The Rule**:
  - Touch velocity is sampled across the final 3 pointer events using an in-place circular velocity tracker (zero heap allocation during drag).
  - On finger release (`MotionEvent.ACTION_UP`), the horizontal fling velocity vector determines the snap target:
    - If velocity $V_x > 500\text{dp/s}$ to the right $\to$ Snap Right.
    - If velocity $V_x < -500\text{dp/s}$ to the left $\to$ Snap Left.
    - If $|V_x| \le 500\text{dp/s}$ $\to$ Snap to whichever edge is horizontally closer.
  - Spring Parameters:
    - **Stiffness**: `Spring.StiffnessMediumLow` ($400f$) for snappy recovery.
    - **Damping Ratio**: `Spring.DampingRatioLowBouncy` ($0.75f$) for an organic, tactile physical bounce upon hitting the edge margin.

---

## 3. Complete UI States Matrix (11 States)

Conforming to the `ui-ux-interaction-laws` specification, every possible user and system state is codified with zero dead-ends:

| State | Trigger / Scenario | Visual Presentation | Primary Action / Recovery |
|---|---|---|---|
| **1. First Use** | App freshly installed, 0 apps whitelisted | Onboarding Wizard with mascot "Bubby" explaining zero-network privacy | Primary Button: `[ Select Chat Apps ]` with 1-tap presets |
| **2. Empty State** | All notifications cleared / read | Bubble overlay vanishes completely; Main settings screen shows sleeping mascot | Quick action: `[ Send Test Bubble ]` to verify overlay function |
| **3. Loading / Arrival** | Incoming message intercepted | Bubble springs onto screen; transient $4\text{s}$ text chip peeks out with sender name | Auto-collapses to $56\text{dp}$ circle after $4\text{s}$ |
| **4. Resting (Idle)** | Bubble docked at screen edge | $56\text{dp}$ avatar, $18\text{dp}$ app source badge, unread badge counter | Single tap to expand preview card; drag to reposition |
| **5. Dragging** | Pointer down + drag past $8\text{dp}$ slop | Bubble depresses to $0.92f$ scale, elevation rises to $6\text{dp}$; dismiss target appears | Drag freely; fling to snap; drop in target to dismiss |
| **6. Magnetized** | Bubble dragged within $100\text{dp}$ of dismiss target | Target expands to $88\text{dp}$, turns red; bubble snaps into center with haptic tick | Release to dismiss; drag away to cancel |
| **7. Expanded Card** | User taps resting bubble | $300\text{dp}$ floating card springs open adjacent to bubble, showing snippet | Action buttons: `[ Reply ]`, `[ Mark Read ]`, `[ Dismiss ]` |
| **8. Inline Reply (IME)** | User taps `[ Reply ]` action | Compact text input appears in card; overlay translates upward above soft keyboard | Keyboard send button sends inline reply via `RemoteInput` |
| **9. Sensitive / OTP** | Banking or 2FA alert intercepted | Message snippet displayed as `"••••••"` in subtle tone | Single tap on card unlocks snippet; settings toggle available |
| **10. Missing Permission** | User revokes overlay or listener in OS | Bubble cleanly disappears; notification shade alerts user with 1-tap direct link | Notification action: `[ Re-enable Overlay Access ]` |
| **11. Edge / Overflow** | Sender has 500-char message or 100+ alerts | Sender name clamped to 1 line (`…`); preview clamped to 2 lines; counter shows `"99+"` | Tap opens underlying app directly for full thread |

---

## 4. Domain Benchmarking & UX Perspective

```
┌─────────────────────────────────────────────────────────────────┐
│                     UX ADVISORY BENCHMARK                       │
├─────────────────────┬───────────────────┬───────────────────────┤
│ ARCHITECTURE        │ COGNITIVE LOAD    │ USER FRICTION         │
├─────────────────────┼───────────────────┼───────────────────────┤
│ Messenger Heads     │ Moderate          │ Accidental back-swipe │
│                     │                   │ during edge drags     │
├─────────────────────┼───────────────────┼───────────────────────┤
│ DynamicSpot         │ High (Constant    │ Blocks status bar     │
│                     │ text at top)      │ icons & clock         │
├─────────────────────┼───────────────────┼───────────────────────┤
│ Our Specification   │ Low (Progressive  │ Zero back-swipe clash │
│                     │ 3-Stage Reveal)   │ (16dp margin law)     │
└─────────────────────┴───────────────────┴───────────────────────┘
```

### Cognitive Load & Friction Analysis

1. **Information Density Balance**:
   - Competing apps either show too much information (DynamicSpot occupies the entire top notch, hiding battery and Wi-Fi indicators) or too little (AOSP Bubbles require two taps and a full window takeover to read a single SMS).
   - Our system uses a **3-stage progressive disclosure**:
     - *Level 0*: $56\text{dp}$ Avatar + Unread Badge (Glanceable in $150\text{ms}$).
     - *Level 1*: $4\text{s}$ transient horizontal peek on arrival (no tap needed for quick context).
     - *Level 2*: Compact $300\text{dp}$ floating card on tap (leaves 70% of screen visible).
2. **Keyboard Ergonomics**:
   - When an inline keyboard opens, standard overlays often lock the screen or cause severe layout jumps. By anchoring to `WindowInsets.Type.ime()`, the transition is synchronized at display refresh rate, eliminating visual stutter.

---

## 5. Production Jetpack Compose Implementation Blueprint

### 5.1 Physics Controller with Edge Snapping & Allocation-Free Tracking

```kotlin
package com.bubble.ui.interaction

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

class BubblePhysicsController(
    private val density: Density,
    private val screenWidthPx: Float,
    private val screenHeightPx: Float,
    private val statusBarHeightPx: Float,
    private val navBarHeightPx: Float
) {
    private val edgeMarginPx = with(density) { 16.dp.toPx() }
    private val bubbleDiameterPx = with(density) { 56.dp.toPx() }
    private val magneticRadiusPx = with(density) { 100.dp.toPx() }

    // Reusable Animatable offset (zero GC allocations per frame)
    val offset = Animatable(
        initialValue = Offset(edgeMarginPx, screenHeightPx * 0.3f),
        typeConverter = Offset.VectorConverter
    )

    val scale = Animatable(1.0f)

    // Bounds Clamping
    val minX: Float get() = edgeMarginPx
    val maxX: Float get() = screenWidthPx - edgeMarginPx - bubbleDiameterPx
    val minY: Float get() = statusBarHeightPx + edgeMarginPx
    val maxY: Float get() = screenHeightPx - navBarHeightPx - bubbleDiameterPx - edgeMarginPx

    // Dismiss target coordinates (bottom center)
    val dismissTargetCenter: Offset
        get() = Offset(
            x = screenWidthPx / 2f,
            y = screenHeightPx - navBarHeightPx - with(density) { 48.dp.toPx() }
        )

    fun checkMagneticZone(currentOffset: Offset): Boolean {
        val bubbleCenter = Offset(
            currentOffset.x + bubbleDiameterPx / 2f,
            currentOffset.y + bubbleDiameterPx / 2f
        )
        val distance = hypot(
            bubbleCenter.x - dismissTargetCenter.x,
            bubbleCenter.y - dismissTargetCenter.y
        )
        return distance <= magneticRadiusPx
    }

    suspend fun snapToNearestEdge(currentVelocityX: Float) {
        val targetX = when {
            currentVelocityX > 500f -> maxX
            currentVelocityX < -500f -> minX
            offset.value.x > (screenWidthPx - bubbleDiameterPx) / 2f -> maxX
            else -> minX
        }
        val clampedY = offset.value.y.coerceIn(minY, maxY)

        offset.animateTo(
            targetValue = Offset(targetX, clampedY),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    suspend fun dismissWithExitAnimation() {
        scale.animateTo(
            targetValue = 0.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
}
```

### 5.2 IME Keyboard Avoidance Modifier

```kotlin
package com.bubble.ui.interaction

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.avoidKeyboard(
    currentBubbleY: Dp,
    screenHeight: Dp,
    bubbleHeight: Dp = 56.dp,
    margin: Dp = 16.dp
): Modifier {
    val imeInsets = WindowInsets.ime.asPaddingValues()
    val imeBottom = imeInsets.calculateBottomPadding()

    val keyboardTop = screenHeight - imeBottom
    val overlap = (currentBubbleY + bubbleHeight) - (keyboardTop - margin)

    val translationY by animateDpAsState(
        targetValue = if (overlap > 0.dp && imeBottom > 0.dp) -overlap else 0.dp,
        animationSpec = spring(),
        label = "ImeBubbleTranslation"
    )

    return this.then(
        Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                placeable.placeRelative(0, translationY.roundToPx())
            }
        }
    )
}
```

---

## 6. Verification & Quality Checklist

- [x] **Law 1**: $16\text{dp}$ gesture safe margin strictly enforced on $X_{\min}$ and $X_{\max}$ to avoid Android back-swipe clashes.
- [x] **Law 2**: `WindowInsets.ime()` reactive translation prevents obscuring soft keyboards.
- [x] **Law 3**: Landscape auto-collapse into a $12\text{dp} \times 36\text{dp}$ side-pill with $0.4f$ opacity.
- [x] **Law 4**: Two-stage magnetic dismiss target zone ($100\text{dp}$ gravitational radius, scale $64\text{dp} \to 88\text{dp}$, haptic tick).
- [x] **Law 5**: Zero-allocation velocity tracking on 120Hz display refresh loops.
- [x] **11 UI States**: Full coverage including first-use onboarding, empty sleeping mascot, sensitive OTP masking, and revoked permissions.
