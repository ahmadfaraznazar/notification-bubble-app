# Product Intake Questionnaire: Notification Bubble App

> **Instructions**: Fill in your thoughts, preferences, and vision directly below each question. You do not need to answer in technical jargon—write in plain English. If you are unsure about any item, leave it blank or write "Help me decide", and we will evaluate it during Phase 1.

---

## 1. Core Vision & Problem Statement

### 1.1 What is the primary problem this app solves?

<!-- Why did you decide to build this app? What frustrates you about current notification handling on your phone? -->

**Answer:**
I have decided to develop this app cause of two main reasons. First being, notification handling, my notification panel that comes when you pull it down from up, is always cluttered and very unreadable and overwhelming. I want to create a alternative way to see my notifications. Second being my love for old school chat head bubbles that was standard in facebook messenger and whatsapp etc and showed your chats on screen in a bubble form. Whether they updated it or changed the standards or put it in opt-in, i dont seem to have them even if my setting are on for bubbles. So i want to create an app that solves these two problems.

There are apps like Chat Heads or so but their ui and ux is very clunky and doesnt feel intuitive and good.

### 1.2 Who is the target user?

<!-- e.g., Power users who multitask while gaming/watching videos, professionals managing multiple chat apps (WhatsApp, Telegram, Slack), or everyday users wanting cleaner notifications? -->

**Answer:**
First of all, I will be very first user of the app. I want to use it myself as well. and target user would be one who, like me, loved chat heads and wants to have them back.

### 1.3 What is the 10-second elevator pitch?

<!-- In 1 or 2 simple sentences, what does this app do for a user? -->

**Answer:**
This app provides old school chat heads way to see notifications for all their apps. Let them select which apps they want to see notifications for and which dont. and also has other accessibility and ways to manage their notifications.

---

## 2. Core Mechanics & Interaction Design

### 2.1 How should floating bubbles appear and behave?

<!-- e.g., Does a bubble pop up immediately when a notification arrives? Does it group by app or by conversation/contact? -->

**Answer:**
A chat bubbles appear immediately when a notification arrives. one can slide it to hide it or expand it to see the full preview. it stacks by apps and by conversations.

### 2.2 What happens when the user interacts with a bubble?

<!--
- Single Tap: (e.g., expand mini preview, open full app, or show quick-reply field?)
- Double Tap: (e.g., dismiss or open?)
- Drag & Drop: (e.g., drag to move freely on screen, auto-snap to edges?)
- Fling / Dismiss: (e.g., drag down to a bottom "X" target zone?)
-->

**Answer:**
On single tap, it expands and shows how many apps have notifications or mainly favorite ones. then clicking on icon it expands to notifications of that app.

### 2.3 What does the expanded overlay look like?

<!--
- A compact floating card with the sender avatar, message snippet, and action buttons?
- A transparent popup with an inline keyboard reply field?
- A full-width bottom sheet?
-->

**Answer:**
A compact floating card with the sender avatar, message snippet, and action buttons.

### 2.4 How should multiple notifications be handled?

<!-- e.g., Stacked bubbles like Facebook Messenger chat heads? A single bubble with an unread badge count? A carousel? -->

**Answer:**
Stacked bubbles like Facebook Messenger chat heads.

---

## 3. Supported Apps & Filtering Rules

### 3.1 Which notifications should trigger bubbles?

<!--
- All messaging apps by default (WhatsApp, Telegram, Signal, SMS)?
- Any app the user manually whitelists?
- Option to exclude ongoing/silent notifications (e.g., music players, download progress)?
-->

**Answer:**
Hybrid Onboarding Wizard with Intelligent Category Filtering:
- **Zero Apps Selected by Default**: On initial launch, 0 apps are enabled by default so the user is never spammed with unexpected bubbles.
- **Onboarding App Picker & Presets**: During first setup, present the user with their installed and most-used communication apps alongside 1-tap presets:
  - `[Chats Only]` (WhatsApp, Telegram, Signal, SMS, Discord, Slack, Messenger)
  - `[Social Media]` (Instagram, X/Twitter, Reddit)
  - `[Custom Selection]` (Searchable checklist of all installed apps)
- **Intelligent Message Classification (Category C Hybrid)**: For selected apps, automatically prioritize person-to-person communication (`Notification.CATEGORY_MESSAGE` and `CATEGORY_CALL`) while ignoring ongoing/silent noise (download progress, music players, marketing promos) unless explicitly overridden.

### 3.2 Should there be smart "Do Not Disturb" or "Auto-Hide" rules?

<!-- e.g., Auto-hide when a full-screen game or YouTube is running in landscape? Auto-hide when the virtual keyboard is open? -->

**Answer:**
Yes, enforce three mandatory OS & interaction avoidance laws:
- **Keyboard Avoidance**: Listen to `WindowInsets.Type.ime()`. When the on-screen keyboard opens, animate bubbles upward to rest cleanly above the keyboard boundary, preventing blocked keystrokes or hidden Send/Backspace buttons.
- **Landscape / Gaming Auto-Collapse**: Automatically collapse bubbles into an ultra-thin, low-opacity side-pill when rotated to landscape or when running full-screen games/media (YouTube, Netflix) to protect HUDs and subtitles.
- **Gesture Navigation Safe Margin**: Enforce a strict `16dp` margin between bubbles and the screen border so Android system edge back-swipes never trigger accidental bubble drags or vice-versa.

---

## 4. Privacy, Security & Sensitive Data

### 4.1 How should private or sensitive notifications be handled?

<!-- e.g., Banking alerts, two-factor OTP codes, password reset texts: should they be masked, ignored, or treated normally? -->

**Answer:**
Option C (Sensitive / OTP Auto-Masking):
- Automatically detect sensitive notifications (banking alerts, 2FA SMS codes, password reset OTPs) and mask the message preview (`"••••••"`) in the floating bubble to protect against shoulder-surfing in public.
- The preview only reveals its text content upon an intentional user tap.
- Provide a settings toggle allowing users to customize or disable sensitive masking per app.

### 4.2 Lock screen & offline policy:

<!-- Should bubbles disappear when the phone is locked? Is everything strictly stored locally on the phone with zero cloud/analytics? -->

**Answer:**
Option A (Zero-Network Architecture for initial release, with future analytics re-evaluation):
- **Zero-Network in v1**: Omit `android.permission.INTERNET` from `AndroidManifest.xml` initially. This provides an absolute on-device guarantee, fast-tracks Google Play policy review, and builds immediate user trust.
- **Future Analytics Re-evaluation**: Re-evaluate adding opt-in, privacy-preserving crash telemetry (e.g., Firebase Crashlytics) in a future update if diagnostic metrics are needed to resolve complex device-specific bugs.
- **Lock Screen Privacy**: Bubbles are automatically dismissed/hidden when the device locks.

### 4.3 Background Reliability & OEM Battery Exemption Policy:
<!-- How will the app prevent background kills by aggressive OEM task managers (Samsung, Xiaomi, OnePlus)? -->

**Answer:**
Option A: Resilient Foreground Service + Guided OEM Exemption:
- **Persistent Foreground Service**: Runs a discrete, low-priority ongoing notification in the notification shade to guarantee Android OS does not kill the process.
- **Guided OEM Setup**: Device brand detection during onboarding with direct deep-links to phone-specific settings (Samsung "Never sleeping apps", Xiaomi "Autostart", etc., conforming to `dontkillmyapp.com` standards).
- **Auto-Revive on Boot**: Includes `RECEIVE_BOOT_COMPLETED` broadcast receiver to re-initialize the bubble service automatically upon phone reboot.

---

## 5. Visual Styling & Customization

### 5.1 What visual aesthetics do you envision?

<!--
- Modern Material 3 (Dynamic Color / Material You)?
- Glassmorphism / blur effects?
- Dark mode first, or dynamic system matching?
-->

**Answer:**
Modern, polished aesthetic with free and premium theme packs:
- **Free Core Themes**: Clean Material 3 (Dynamic Color / Material You palette) and standard Dark/Light mode matching.
- **Premium Themes (Pro Unlock)**: Frosted Glass / Glassmorphism, OLED Pitch Black, Cyberpunk Neon, and app-accented glow borders.

### 5.2 What level of customization should the user have?

<!-- e.g., Bubble size, badge colors, border thickness, opacity/transparency, custom notification sounds or haptics? -->

**Answer:**
Deep customization with tiered access:
- **Free Tier**: Default bubble size, standard badge indicators, drag snapping behavior.
- **Pro Tier**: Continuous slider for bubble size (Small / Medium / Large), custom badge colors, border thickness, transparency/opacity levels, and per-app snooze schedules.

---

## 6. Competitive Differentiation & Gold Standards

### 6.1 What apps do you consider the standard or benchmark?

<!-- e.g., Facebook Messenger Chat Heads, Android Native Bubbles, DynamicSpot, Flyperlink, or something else? -->

**Answer:**
Facebook Messenger Chat Heads comes to mind.

### 6.2 What is your "unfair advantage" or unique twist?

<!-- Why will someone use this app instead of the built-in system notification drawer or existing apps on Google Play? -->

**Answer:**
Old school nostalgia. Customization. Modern UI. Privacy. And yet to be planned notification mangement.

---

## 7. Business & Monetization Strategy (If applicable)

### 7.1 What is your planned monetization model?

<!--
- 100% Free & Open Source?
- Freemium (Core features free, Pro tier for unlimited apps/custom themes)?
- One-time purchase or subscription?
-->

**Answer:**
Option A: Hybrid Freemium with One-Time Lifetime In-App Purchase ($3.99 – $4.99):
- **Free Tier (Fully functional core utility)**:
  - Up to 2–3 active simultaneous apps.
  - Core bubble drag physics, screen-edge snapping, and basic notification previews.
  - Clean Material 3 / Light & Dark system themes.
- **Pro Tier ($3.99 – $4.99 single lifetime unlock, zero subscriptions)**:
  - Unlimited simultaneous app bubbles.
  - Complete premium visual theme pack (OLED Pitch Black, Cyberpunk Neon, Frosted Glass).
  - Deep customization (custom sizing, per-app snooze rules, custom quick-reply templates).
  - One-time payment builds long-term goodwill and maximizes conversion among Android power users.

---

## 8. Development Constraints & Scope for MVP (Phase 1)

### 8.1 What is the single MUST-HAVE feature for the first testable version?

<!-- If we could only ship ONE working interaction next week, what is it? -->

**Answer:**
Basic notification bubble appearance and interaction.

### 8.2 What are explicit NON-GOALS for the MVP?

<!-- What features should we deliberately avoid building right now to ship faster? -->

**Answer:**
i dont know yet.
