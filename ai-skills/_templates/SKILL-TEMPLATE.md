---
name: skill-name-kebab-case
description: Use this skill when <concise trigger scenario and operational context, e.g., auditing an architecture proposal, generating a PRD, or running a pre-flight store check>.
---

# <Skill Title>

## 1. Role & Boundary
- **Core Mission**: <One single focused sentence explaining what this skill executes>.
- **Scope**:
  - <In-scope action item 1>
  - <In-scope action item 2>
  - <In-scope action item 3>
- **Explicit Non-Goals**:
  - Do NOT <boundary 1 - e.g., generate full implementation code when acting as auditor>.
  - Do NOT <boundary 2 - e.g., make vague subjective claims without actionable alternatives>.
  - Do NOT <boundary 3 - e.g., proceed with unstated assumptions instead of flagging risks>.

---

## 2. Step-by-Step Workflow
1. **Context Ingestion & Intake Validation**:
   - Check input completeness against requirements (<prerequisites>).
   - If critical constraints are missing, halt or identify default fallback assumptions explicitly.
2. **Analysis / Execution Phase**:
   - Step 2.1: <Primary operational pass>.
   - Step 2.2: <Secondary stress-test / edge-case analysis>.
   - Step 2.3: <Synthesis of actionable findings>.
3. **Synthesis & Contract Verification**:
   - Format findings strictly against the Output Contract.
   - Verify every item has an actionable owner, priority level, and rationale.

---

## 3. Hard Laws & Anti-Patterns
- **LAW 1**: <Absolute invariant rule 1>.
- **LAW 2**: <Absolute invariant rule 2>.
- **FORBIDDEN**: Never <anti-pattern 1, e.g., suggest generic boilerplate without project context>.
- **FORBIDDEN**: Never <anti-pattern 2, e.g., omit trade-offs when recommending third-party dependencies>.
- **FORBIDDEN**: Never <anti-pattern 3, e.g., provide unranked lists of issues without severity scoring>.

---

## 4. Output Contract
The response MUST follow this exact structure:

```markdown
### Executive Summary
- **Verdict / Status**: [PASS | FAIL | CONDITIONAL PASS]
- **Primary Finding**: <1-2 sentences on critical takeaway>

### Actionable Findings / Breakdown
| ID | Area / Component | Severity (P0/P1/P2) | Finding & Impact | Prescribed Action |
|---|---|---|---|---|
| 01 | <Area> | P0 | <Issue description> | <Concrete fix> |

### Decision / Trade-Off Log
- **Chosen Direction**: <Selected strategy>
- **Rejected Alternatives**: <Alternative 1 (reason rejected), Alternative 2 (reason rejected)>
- **Residual Risks**: <Known risks and mitigation path>

### Next Operational Steps
1. [ ] <Immediate next step 1>
2. [ ] <Immediate next step 2>
```
