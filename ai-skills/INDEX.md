# Master AI Skills Library (`ai-skills-library`)

> Local repository of battle-tested, high-leverage agent skills conforming to the Agent Skills Open Standard (`SKILL.md`).
> **Domain 01: Application Development is 100% complete (21/21 skills populated from real-world repositories).**

---

## Routing & Directory Architecture

```
ai-skills-library/
├── INDEX.md                         # Master routing table (this file)
├── sync-skills.sh                   # Symlink utility into active agent skill directories
├── _templates/
│   └── SKILL-TEMPLATE.md            # Canonical standard for generating new skills
├── 01-app-development/              # Full application development lifecycle (21/21 Complete)
│   ├── 01-ideation-and-validation/  # Idea stress-testing, market gaps, unit economics (4/4)
│   ├── 02-planning-and-pm/          # Scoping, PRDs, roadmaps, atomic tasks (3/3)
│   ├── 03-architecture-and-tech/    # System design, tech selection, platform standards (5/5)
│   ├── 04-design-and-ux/            # Design systems, interaction laws, accessibility (3/3)
│   ├── 05-growth-and-monetization/  # Pricing, paywalls, distribution, conversion copy (3/3)
│   └── 06-engineering-ops/          # Code review, repo standards, release gates (3/3)
├── 02-creative-and-design/          # 3D, UI/UX systems, visual direction, worldbuilding
├── 03-learning-and-research/        # Technical research, paper breakdowns, curricula
├── 04-career-and-business/          # Resumes, negotiations, proposals, positioning
└── 05-personal-ops/                 # Life ops, habit architectures, decision audits
```

---

## Domain 01: Application Development (`01-app-development/`) — 21/21 Complete

### Phase 1: Ideation & Validation (`01-ideation-and-validation/`)
| Skill Name | Folder Path | Primary Trigger | Status / Provenance |
|---|---|---|---|
| `idea-brutal-audit` | [`01-app-development/01-ideation-and-validation/idea-brutal-audit`](./01-app-development/01-ideation-and-validation/idea-brutal-audit/SKILL.md) | Adversarial VC critique, pre-mortem, substitution test | **Installed** (`idea-evaluator-con` / Prince-1652) |
| `market-research-scout` | [`01-app-development/01-ideation-and-validation/market-research-scout`](./01-app-development/01-ideation-and-validation/market-research-scout/SKILL.md) | Competitor mining, 1-star review gaps, pricing analysis | **Installed** (`competitor-gap-analyzer` / 100seotools) |
| `genre-trends-radar` | [`01-app-development/01-ideation-and-validation/genre-trends-radar`](./01-app-development/01-ideation-and-validation/genre-trends-radar/SKILL.md) | Latest mechanics, onboarding UX, and timing in target genre | **Installed** (`everythingclaudecode` / ECC) |
| `feasibility-unit-economics` | [`01-app-development/01-ideation-and-validation/feasibility-unit-economics`](./01-app-development/01-ideation-and-validation/feasibility-unit-economics/SKILL.md) | CAC/LTV stress test, scaling bottlenecks, financial projections | **Installed** (`startup-financial-modeling`) |

---

### Phase 2: Planning & Product Management (`02-planning-and-pm/`)
| Skill Name | Folder Path | Primary Trigger | Status / Provenance |
|---|---|---|---|
| `prd-and-scope-cutter` | [`01-app-development/02-planning-and-pm/prd-and-scope-cutter`](./01-app-development/02-planning-and-pm/prd-and-scope-cutter/SKILL.md) | MVP scoping, aggressive non-goals, and feature cuts | **Installed** (`to-prd` / Matt Pocock) |
| `roadmap-and-milestones` | [`01-app-development/02-planning-and-pm/roadmap-and-milestones`](./01-app-development/02-planning-and-pm/roadmap-and-milestones/SKILL.md) | Phased delivery, risk registers, and definition of done | **Installed** (`roadmap-planner` / o2alexanderfedin) |
| `spec-driven-tasks` | [`01-app-development/02-planning-and-pm/spec-driven-tasks`](./01-app-development/02-planning-and-pm/spec-driven-tasks/SKILL.md) | Atomic, testable task cards designed for coding agents | **Installed** (`spec-driven-development` / Addy Osmani) |

---

### Phase 3: Architecture & Tech (`03-architecture-and-tech/`)
| Skill Name | Folder Path | Primary Trigger | Status / Provenance |
|---|---|---|---|
| `clean-architecture-enforcer` | [`01-app-development/03-architecture-and-tech/clean-architecture-enforcer`](./01-app-development/03-architecture-and-tech/clean-architecture-enforcer/SKILL.md) | Strict layer boundaries, dumb UI, dependency inversion | **Installed** (`architecture-patterns` + playbook) |
| `tech-and-library-selector` | [`01-app-development/03-architecture-and-tech/tech-and-library-selector`](./01-app-development/03-architecture-and-tech/tech-and-library-selector/SKILL.md) | Stack trade-off matrix, lock-in evaluation, TCO & perf audits | **Installed** (`tech-stack-evaluator` / borghei + scripts) |
| `android-best-practices` | [`01-app-development/03-architecture-and-tech/android-best-practices`](./01-app-development/03-architecture-and-tech/android-best-practices/SKILL.md) | Jetpack Compose UDF, offline-first, Play Store policy | **Installed** (`android-jetpack-compose-expert`) |
| `api-and-data-modeling` | [`01-app-development/03-architecture-and-tech/api-and-data-modeling`](./01-app-development/03-architecture-and-tech/api-and-data-modeling/SKILL.md) | Schema design, indexing, caching, zero-downtime migrations | **Installed** (`database-architect`) |
| `security-and-privacy-audit` | [`01-app-development/03-architecture-and-tech/security-and-privacy-audit`](./01-app-development/03-architecture-and-tech/security-and-privacy-audit/SKILL.md) | Auth/RBAC, OWASP mobile/web, secrets management, GDPR | **Installed** (`vulnerability-scanner` + scripts) |

---

### Phase 4: Design & UX (`04-design-and-ux/`)
| Skill Name | Folder Path | Primary Trigger | Status / Provenance |
|---|---|---|---|
| `persistent-design-system` | [`01-app-development/04-design-and-ux/persistent-design-system`](./01-app-development/04-design-and-ux/persistent-design-system/SKILL.md) | Design tokens, spacing scales, typography, core primitives | **Installed** (`web-design-guidelines` / Vercel Labs) |
| `ui-ux-interaction-laws` | [`01-app-development/04-design-and-ux/ui-ux-interaction-laws`](./01-app-development/04-design-and-ux/ui-ux-interaction-laws/SKILL.md) | State machines (empty/loading/error), destructive dialogs | **Installed** (`ui-states`) |
| `accessibility-a11y-audit` | [`01-app-development/04-design-and-ux/accessibility-a11y-audit`](./01-app-development/04-design-and-ux/accessibility-a11y-audit/SKILL.md) | WCAG AAA contrast, TalkBack/Screen Reader, 48dp touch targets | **Installed** (`accessibility-compliance-accessibility-audit`) |

---

### Phase 5: Growth & Monetization (`05-growth-and-monetization/`)
| Skill Name | Folder Path | Primary Trigger | Status / Provenance |
|---|---|---|---|
| `monetization-and-paywalls` | [`01-app-development/05-growth-and-monetization/monetization-and-paywalls`](./01-app-development/05-growth-and-monetization/monetization-and-paywalls/SKILL.md) | Pricing tiers, paywall placement, retention & churn loops | **Installed** (`paywall-upgrade-cro` + guide) |
| `go-to-market-distribution` | [`01-app-development/05-growth-and-monetization/go-to-market-distribution`](./01-app-development/05-growth-and-monetization/go-to-market-distribution/SKILL.md) | ASO/SEO keyword intent, launch playbooks, store ranking | **Installed** (`app-store-optimization` + tools) |
| `copywriting-and-positioning` | [`01-app-development/05-growth-and-monetization/copywriting-and-positioning`](./01-app-development/05-growth-and-monetization/copywriting-and-positioning/SKILL.md) | Landing page copy, onboarding friction-killers, objection handling | **Installed** (`boring-marketing-positioning-angles` + frameworks) |

---

### Phase 6: Engineering Ops (`06-engineering-ops/`)
| Skill Name | Folder Path | Primary Trigger | Status / Provenance |
|---|---|---|---|
| `repo-map-and-conventions` | [`01-app-development/06-engineering-ops/repo-map-and-conventions`](./01-app-development/06-engineering-ops/repo-map-and-conventions/SKILL.md) | Repository laws, file naming, structured logging, CI setup | **Installed** (`cc-skill-coding-standards` + guide) |
| `code-review-adversary` | [`01-app-development/06-engineering-ops/code-review-adversary`](./01-app-development/06-engineering-ops/code-review-adversary/SKILL.md) | Rigorous PR edge-case dissection, perf leaks, zero fluff | **Installed** (`five-axis-code-review` / alapha888) |
| `release-and-qa-checklist` | [`01-app-development/06-engineering-ops/release-and-qa-checklist`](./01-app-development/06-engineering-ops/release-and-qa-checklist/SKILL.md) | Store release verification, rollback plans, crash gate checks | **Installed** (`deployment-procedures`) |

---

## Future Domains (02–05)
- [`02-creative-and-design/README.md`](./02-creative-and-design/README.md)
- [`03-learning-and-research/README.md`](./03-learning-and-research/README.md)
- [`04-career-and-business/README.md`](./04-career-and-business/README.md)
- [`05-personal-ops/README.md`](./05-personal-ops/README.md)
