# SDD Interaction Protocol
**Related**: [constitution.md](../constitution.md) | [sdd.md](../architecture/sdd.md) | [feature-development.md](./feature-development.md)

---

## Purpose

Define the exact protocol for how AI coding agents interact with the SDD specification system. This document governs agent behavior from the moment a task is received to the moment code is submitted.

---

## Agent Initialization Protocol

Every time an agent receives a task, it must execute this initialization sequence **before** generating any code:

```
INITIALIZATION SEQUENCE
========================
1. Read constitution.md          → Load Prime Directives
2. Read applicable rules files   → Load relevant architectural rules
3. Read .ai/specs/decisions/     → Load all accepted ADRs
4. Read feature spec (if exists) → Load feature-specific requirements
5. Check folder structure        → Identify affected files
6. Validate no conflicts         → Confirm task is compliant
7. Begin implementation          → Generate code
```

---

## How Agents Parse `.ai/specs/`

### Feature Specs

Feature specs live in `.ai/specs/features/{feature-name}.md`. Before implementing any feature, parse:

```
Feature Spec Structure (expected):
├── Overview — what the feature does
├── User stories — who needs what and why
├── Acceptance criteria — exactly what "done" means
├── API contracts — endpoint shapes, request/response
├── Data model — entity fields and relationships
├── Edge cases — known exceptions and how to handle them
└── Out of scope — explicitly what is NOT included
```

**Agent action**: Extract each section and map it to the corresponding implementation layer.

| Spec Section | Implementation Target |
|---|---|
| Data model | Domain Entities |
| API contracts | DTOs + DataSource methods |
| Business rules (from user stories) | Use Cases |
| Acceptance criteria | Test cases |
| User stories (UI behavior) | Screens + Notifier state |

---

## Spec Compliance Verification

Before submitting output, an agent must verify each acceptance criterion is met:

```
For each acceptance criterion in the feature spec:
  └── Is there a test that validates this criterion?
      ├── YES → verified
      └── NO  → BLOCKED — write the test first
```

**Example**:

Feature spec states:
> "AC-03: A room cannot be marked as 'Available' if it has an active booking."

Agent must verify:
```dart
// ✅ Test exists for AC-03
test('returns ConflictFailure when marking available with active booking', () async {
  when(() => bookingRepo.hasActiveBookingForRoom('room-1'))
      .thenAnswer((_) async => true);

  final result = await useCase.execute(MarkAvailableParams(roomId: 'room-1'));

  expect(result.fold((f) => f, (_) => null), isA<ConflictFailure>());
});
```

---

## Rule Verification Sequence

Before outputting any code file, verify against each relevant rule:

```dart
// Agent internal checklist (must be satisfied before output):
final ruleChecks = [
  // From constitution.md
  'PD1: No layer boundary violations',
  'PD2: All business logic is unit-testable',
  'PD3: No magic strings — all identifiers are constants',
  'PD4: Code follows specs exactly — no autonomous deviations',
  'PD5: Uses established patterns — no novel inventions',

  // From clean-architecture.md
  'No Presentation importing Data',
  'No Domain importing infrastructure',
  'All repo methods return Either<Failure, T>',

  // From riverpod.md
  'All providers use @riverpod annotation',
  'ref.watch only in build()',
  'ref.read only in action methods',

  // From dependency-rules.md
  'No forbidden imports',
];
```

---

## Blocking Conditions

An agent **must stop and report** (not continue) when any of the following are true:

| Condition | Agent Action |
|---|---|
| Feature spec does not exist | Request the spec before proceeding |
| Task requires a new package | Draft ADR, stop implementation |
| Task requires deviating from a rule | Draft ADR, stop implementation |
| Acceptance criterion has no corresponding test | Write the test first |
| Two rules conflict | Flag conflict, request human resolution |
| Feature touches a sealed ADR that was rejected | Cannot proceed — escalate |

```
BLOCKING CONDITION DETECTED
============================
⚠️  AGENT OUTPUT BLOCKED

Reason: Task requires introducing 'get_it' package for DI,
        which conflicts with the Riverpod-only DI rule in dependency-rules.md.

Action taken:
  - Created ADR draft: .ai/specs/decisions/2026-06-01-add-get-it-for-di.md
  - Status: Proposed (awaiting architect review)

Implementation: BLOCKED until ADR-007 is accepted or rejected.

Unblocked tasks: [Room list widget, Room detail screen layout]
Continuing with unblocked tasks...
```

---

## Agent Output Format

Every agent response that produces code must follow this structure:

```
1. SUMMARY
   - What was implemented
   - Which spec sections are covered
   - Which acceptance criteria are now verifiable

2. FILES CREATED/MODIFIED
   - List every file with its path
   - One-line description of changes

3. TESTS WRITTEN
   - List every test file with test count
   - Coverage estimate for affected code

4. OPEN ITEMS (if any)
   - Unresolved questions
   - Blocked tasks with reason
   - ADR drafts created

5. VERIFICATION CHECKLIST
   - Agent self-assessment against relevant rules
```

---

## Spec Update Protocol

When implementation reveals that a spec is incomplete or incorrect:

```
1. Do NOT deviate from the spec silently
2. Document the gap as an "OPEN ITEM" in the output
3. If blocking: stop and request spec update
4. If non-blocking: implement to the best interpretation and flag it

Example flag:
  ⚠️ SPEC GAP DETECTED
  Feature spec (room-management.md, AC-07) does not specify
  what happens when a room is deleted while a pending booking exists.
  Assumed behavior: Return ConflictFailure with message "Room has pending booking."
  Please update the spec to confirm or correct this assumption.
```

---

## Agent Verification Checklist

- [ ] Initialization sequence completed: constitution.md → rules → ADRs → feature spec → folder check.
- [ ] Every acceptance criterion in the feature spec has a corresponding test.
- [ ] All rule verification checks pass before code is output.
- [ ] Blocking conditions are reported immediately with an ADR draft if applicable.
- [ ] Output follows the structured format: Summary → Files → Tests → Open Items → Checklist.
- [ ] No code deviates from specs without explicit flagging in "Open Items".
- [ ] Spec gaps are documented — never silently filled with agent's own judgment.
- [ ] ADR drafts are created for any pattern deviation — never autonomously accepted.
- [ ] Agent does not self-approve ADRs. Status remains `Proposed` until human review.
- [ ] Final output references the spec sections that justify each architectural decision.
