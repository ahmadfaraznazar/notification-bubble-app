# Technology & Library Selection Matrix: Notification Bubble App

> **Objective**: Evidence-backed technology comparison, dependency version pinning, and binary footprint audit to enforce our < 5MB APK size and 120Hz performance targets.  
> **Skill Activated**: `tech-and-library-selector` ([SKILL.md](file:///Users/ahmadnazar/Documents/GitHub/notification-bubble-app/ai-skills/01-app-development/03-architecture-and-tech/tech-and-library-selector/SKILL.md))  
> **Date**: 2026-11-22  

---

## 1. Executive Summary & Verdict

- **Stack Strategy**: **Ultra-Lean Modern Android (Kotlin 2.0 + Jetpack Compose + Room + KSP)**
- **Binary Footprint Target**: Base APK strictly under **5.0 MB** (Competitors: 25MB–35MB).
- **Core Architectural Win**:
  - Zero network libraries (`OkHttp`, `Retrofit`) omitted &mdash; saves ~5MB.
  - Coil Compose over Glide/Fresco &mdash; saves ~4MB.
  - Pure Vector Drawables over multi-density PNGs &mdash; saves ~3MB.
  - KSP compile-time code generation over Kapt/Reflection &mdash; zero runtime reflection overhead and 2x faster builds.

---

## 2. Multi-Criteria Stack Comparison Matrix

We evaluated competing technologies across our 4 core architectural constraints: **Binary Size**, **Frame Rate (120Hz)**, **RAM Overhead**, and **Developer Experience (React Analogy)**.

| Component Area | Candidates Evaluated | Winner | Key Deciding Evidence |
|---|---|:---:|---|
| **UI Framework** | 1. **Jetpack Compose (BOM 2024.06.00)**<br>2. Legacy XML Views (`WindowManager.updateViewLayout`) | **Jetpack Compose** | Legacy `updateViewLayout` triggers heavy CPU measure/layout passes on every drag event (stutters on 120Hz OLEDs). Compose `graphicsLayer` translates on the GPU at 0% CPU cost and mirrors React functional components. |
| **Local Persistence** | 1. **Room SQLite 2.6.1 (with KSP)**<br>2. Raw `SQLiteOpenHelper`<br>3. Realm / ObjectBox | **Room SQLite** | Realm bundles 4MB+ native C++ binaries (kills <5MB goal). Raw SQLite lacks compile-time verification. Room with KSP is type-safe, adds only ~120KB, and natively emits `Flow<T>` for Compose. |
| **Image Loading** | 1. **Coil Compose 2.6.0**<br>2. Glide 4.16<br>3. Fresco (Meta) | **Coil Compose** | Fresco adds 6MB+; Glide adds 3.5MB+ and requires View interop. Coil is 100% Kotlin/Coroutines, built for Compose, memory-efficient, and weighs only **~150KB**. |
| **Code Generation** | 1. **KSP (Kotlin Symbol Processing)**<br>2. Kapt (Annotation Processing) | **KSP** | Kapt generates Java stubs and adds 15–25 seconds to compile times. KSP operates directly on Kotlin AST, compiles 2x faster, and has zero runtime footprint. |
| **Monetization** | 1. **Google Play Billing KTX 6.2.1**<br>2. RevenueCat SDK | **Play Billing KTX** | RevenueCat requires network permissions and cloud telemetry (violating our v1 Zero-Network privacy guarantee). Official Play Billing operates natively on-device. |

---

## 3. Pinned Dependency Bill of Materials (BOM)

These exact stable versions will be declared in `gradle/libs.versions.toml` and `build.gradle.kts`:

### A. Core Platform & Plugins
```toml
[versions]
agp = "8.5.1"
kotlin = "2.0.0"
ksp = "2.0.0-1.0.22"
compose-bom = "2024.06.00"
room = "2.6.1"
coil = "2.6.0"
play-billing = "6.2.1"
coroutines = "1.8.1"
lifecycle = "2.8.3"
```

### B. Dependency Declarations & Size Audit

| Library Artifact | Purpose | Estimated Size | Justification |
|---|---|:---:|---|
| `androidx.compose:compose-bom:2024.06.00` | Compose Version Alignment | &mdash; | Pins all Compose libraries to mutually tested stable releases. |
| `androidx.compose.ui:ui` | Compose Core & Graphics | ~1.8 MB | Hardware-accelerated Canvas & gesture detection. |
| `androidx.compose.material3:material3` | Material You Dynamic Theming | ~1.2 MB | Palette generation, theme tokens, and typography. |
| `androidx.compose.animation:animation` | Spring Physics Engine | ~350 KB | Damped spring curves and magnetic pull math. |
| `androidx.room:room-runtime:2.6.1` | Local SQLite ORM | ~120 KB | App whitelist and persistent settings storage. |
| `androidx.room:room-ktx:2.6.1` | Coroutines & Flow Support | ~45 KB | Exposes reactive database streams to Use Cases. |
| `io.coil-kt:coil-compose:2.6.0` | In-Memory Avatar Loading | ~150 KB | Async circular avatar caching without network bloat. |
| `com.android.billingclient:billing-ktx:6.2.1` | In-App Purchases (Pro Unlock) | ~200 KB | Google Play v6 billing contract. |
| `androidx.lifecycle:lifecycle-service:2.8.3` | Foreground Service Lifecycle | ~60 KB | Ties `OverlayService` cleanly to Android lifecycle. |
| **Total Estimated Uncompressed Code** | &mdash; | **~3.9 MB** | **Fits strictly inside the < 5.0 MB target.** |

---

## 4. Explicitly Forbidden Dependencies (The "Blacklist")

To prevent accidental bloat, the following libraries are **permanently banned** from `build.gradle.kts`:

1. ❌ **`com.squareup.okhttp3:*` / `com.squareup.retrofit2:*`**: No network socket or HTTP clients allowed in v1.0.
2. ❌ **`com.google.firebase:*`**: No remote telemetry or cloud services allowed in v1.0.
3. ❌ **`io.realm:*` / `io.objectbox:*`**: Heavy C++ database engines strictly forbidden.
4. ❌ **`com.github.bumptech.glide:*`**: Heavy legacy image loader rejected in favor of Coil.
5. ❌ **`androidx.compose.material:material-icons-extended`**: Giant 25MB icon library; we import only single vector XML icons.

---

## 5. Next Steps for Step 3.3

With dependencies pinned and size constraints locked:
- In **Step 3.3 (`android-best-practices`)**, we establish the Android lifecycle rules, `NotificationListenerService` isolation, and 120Hz memory leak prevention guidelines.
