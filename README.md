# Notification Bubble App

> A modern, high-performance floating notification bubble application for Android, delivering seamless multitasking, quick previews, and inline responses without disrupting the active workflow.

---

## Overview

The **Notification Bubble App** captures incoming notifications and presents them as interactive floating bubbles (chat heads / dynamic overlays), enabling users to:
- Preview incoming messages and alerts instantaneously.
- Reply inline without leaving full-screen apps or games.
- Group and manage active conversation threads with physics-based drag-and-drop gestures.
- Filter, snooze, and prioritize notifications based on custom application rules.

---

## AI Development Skills Suite (`ai-skills/`)

This repository is equipped with an integrated library of **21 specialized AI Agent Skills** adhering to the Agent Skills Open Standard (`SKILL.md`). Configured via [`.agents/skills.json`](.agents/skills.json), these skills automatically empower AI coding agents throughout the entire engineering lifecycle:

```text
ai-skills/
├── INDEX.md                         # Skills routing table & documentation
├── sync-skills.sh                   # Global CLI symlink utility
└── 01-app-development/
    ├── 01-ideation-and-validation/  # Idea stress-testing, market gaps, unit economics
    ├── 02-planning-and-pm/          # PRD scoping, milestone roadmaps, spec tasks
    ├── 03-architecture-and-tech/    # Clean architecture, Jetpack Compose, security/privacy
    ├── 04-design-and-ux/            # Design tokens, interaction physics, accessibility (a11y)
    ├── 05-growth-and-monetization/  # Monetization, paywalls, ASO distribution
    └── 06-engineering-ops/          # Code review adversary, conventions, release gates
```

### Key Skills for This App

| Area | Recommended Skill | Trigger / Purpose |
|---|---|---|
| **Android Core** | `android-best-practices` | Jetpack Compose UDF, `NotificationListenerService`, `BubbleMetadata`, Foreground Services |
| **Privacy & Security** | `security-and-privacy-audit` | Auditing sensitive notification payloads, OTP redaction, zero external leak |
| **Interaction & UX** | `ui-ux-interaction-laws` | Drag-to-dismiss mechanics, screen-edge snapping, spring animations, overlay states |
| **Design System** | `persistent-design-system` | Badge counters, avatar styling, theme support (Dark/Light/Dynamic) |
| **Architecture** | `clean-architecture-enforcer` | Layer boundaries between Notification Services, Storage (Room), and UI |

---

## Project Structure

```text
notification-bubble-app/
├── .agents/                         # Antigravity agent configuration
│   └── skills.json                  # Workspace skill registration
├── ai-skills/                       # Agent Skills library (21 skills across 6 phases)
├── documents/                       # Phase-organized product documentation
│   ├── PROJECT_PROGRESS_TRACKER.md  # Master real-time progress dashboard
│   ├── PRODUCT_INTAKE_QUESTIONNAIRE.md
│   ├── 01-ideation-and-validation/  # Phase 1 Strategy Artifacts
│   │   ├── IDEA_BRUTAL_AUDIT.md
│   │   ├── MARKET_RESEARCH_GAP_ANALYSIS.md
│   │   ├── GENRE_TRENDS_RADAR.md
│   │   ├── FEASIBILITY_UNIT_ECONOMICS.md
│   │   └── MONETIZATION_STRATEGY_OPTIONS.md
│   ├── 02-planning-and-pm/          # Phase 2 Specifications (PRD, Roadmap, Specs)
│   │   ├── PRODUCT_REQUIREMENTS_DOCUMENT.md
│   │   ├── ROADMAP_AND_MILESTONES.md
│   │   └── SPEC_DRIVEN_TASK_CARDS.md
│   └── 03-architecture-and-tech/    # Phase 3 Architecture & Technical Contracts
│       ├── CLEAN_ARCHITECTURE_SPECIFICATION.md
│       └── TECH_AND_LIBRARY_SELECTION.md
├── AGENTS.md                        # Agent guidelines & commit conventions
├── .gitignore                       # Git ignore rules for Android, Python, and OS junk
└── README.md                        # Project documentation
```

---

## License

All rights reserved.
