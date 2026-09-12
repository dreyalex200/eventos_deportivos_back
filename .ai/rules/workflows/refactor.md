# Safe Refactoring Workflow
**Related**: [bug-fix.md](./bug-fix.md) | [testing.md](../mobile/testing.md) | [constitution.md](../constitution.md) | [sdd.md](../architecture/sdd.md)

---

## Purpose

Define the safe refactoring protocol. Refactoring without tests is gambling. Every refactor follows the same safety net sequence: establish tests → refactor in small steps → verify.

---

## The Golden Rule

> **Never refactor and add features simultaneously.**
> A refactor PR changes structure without changing behavior. A feature PR adds behavior.
> These are separate commits, separate PRs.

---

## Pre-Refactor Checklist

Before touching any code:

```
□ Does the code to be refactored have adequate test coverage?
  └── YES → proceed to Step 1
  └── NO  → first write tests until coverage ≥ 80% for the affected code
             (this is the "characterization test" step)

□ Is this a large refactor affecting multiple features?
  └── YES → create ADR first, get architect approval, then proceed
  └── NO  → proceed to Step 1

□ Are there any in-progress PRs that touch the same files?
  └── YES → coordinate with the team to avoid merge conflicts
  └── NO  → proceed to Step 1
```

---

## Step 1: Establish the Safety Net

Write "characterization tests" that capture the current (possibly imperfect) behavior. These tests will break if the refactor accidentally changes behavior.

```dart
// Before refactoring RoomRepositoryImpl, write tests that capture its current behavior:

group('RoomRepositoryImpl — pre-refactor characterization', () {
  test('getById: calls remote and caches result', () async { ... });
  test('getById: falls back to cache on network error', () async { ... });
  test('getRooms: returns empty list if both remote and cache fail', () async { ... });
  // Cover every public method and its observable behaviors
});

// Run and confirm all tests pass BEFORE making any changes:
// flutter test test/features/rooms/data/repositories/room_repository_impl_test.dart
```

---

## Step 2: Plan in Small Steps

Break the refactor into the smallest possible independent changes. Each step must:
- Leave the tests passing
- Be committable on its own

**Example: Refactoring from manual JSON parsing to `json_serializable`**

```
Step A: Add json_serializable dependency and annotations (no behavior change)
Step B: Generate code, verify generated fromJson matches manual parsing
Step C: Replace manual fromJson with generated one in ONE DTO
Step D: Run tests → green → commit
Step E: Replace next DTO
Step F: Run tests → green → commit
... repeat until complete
Step Z: Remove old manual parsing code
```

---

## Step 3: Apply Each Step

```dart
// BEFORE: Manual parsing (being refactored)
final class RoomDto {
  factory RoomDto.fromJson(Map<String, dynamic> json) {
    return RoomDto(
      id: json['id'] as String,
      number: json['number'] as String,
      pricePerNight: (json['price_per_night'] as num).toDouble(),
    );
  }
}

// AFTER: Generated (one DTO at a time)
@JsonSerializable()
final class RoomDto {
  const RoomDto({...});

  @JsonKey(name: 'price_per_night')
  final double pricePerNight;

  factory RoomDto.fromJson(Map<String, dynamic> json) =>
      _$RoomDtoFromJson(json); // Generated

  Room toDomain() => Room(...); // Unchanged
}
```

After each step:
```bash
flutter test test/features/rooms/  # Must be green
flutter analyze                    # Must be zero errors
```

---

## Step 4: Validate with Linter and Static Analysis

After all refactor steps are complete:

```bash
# Full analysis
flutter analyze --fatal-infos --fatal-warnings

# Import violations
dart run custom_lint

# Full test suite
flutter test

# Coverage check
flutter test --coverage
genhtml coverage/lcov.info -o coverage/html
# Verify coverage didn't drop
```

---

## Common Refactor Scenarios

### Extracting a Use Case from a Fat Notifier

```dart
// BEFORE: Business logic in Notifier (violation)
@riverpod
class RoomNotifier extends _$RoomNotifier {
  Future<void> markUnavailable(String id) async {
    // ❌ Business logic in Notifier
    if (await hasActiveBooking(id)) {
      throw Exception('Cannot mark as unavailable — active booking exists');
    }
    await ref.read(roomRepositoryProvider).updateStatus(id, RoomStatus.maintenance);
  }
}

// AFTER: Business logic moved to UseCase
final class MarkRoomUnavailableUseCase {
  const MarkRoomUnavailableUseCase(this._roomRepo, this._bookingRepo);

  Future<Either<Failure, void>> execute(String roomId) async {
    final hasBooking = await _bookingRepo.hasActiveBookingForRoom(roomId);
    if (hasBooking) {
      return const Left(ConflictFailure(message: 'Room has active booking'));
    }
    return _roomRepo.updateStatus(roomId, RoomStatus.maintenance);
  }
}

// Notifier becomes thin — only calls UseCase
@riverpod
class RoomNotifier extends _$RoomNotifier {
  Future<void> markUnavailable(String id) async {
    final result = await ref.read(markRoomUnavailableUseCaseProvider).execute(id);
    result.fold(
      (f) => ref.read(snackbarNotifierProvider.notifier).showError(f.message),
      (_) => ref.invalidateSelf(),
    );
  }
}
```

### Moving Widget Logic to Entity

```dart
// BEFORE: Presentation logic in Widget
Text(
  room.status == RoomStatus.available
      ? 'Available'
      : room.status == RoomStatus.occupied
          ? 'Occupied'
          : 'Other',
)

// AFTER: Domain entity provides the label
// In Room entity (domain layer):
extension RoomStatusLabel on RoomStatus {
  String get label => switch (this) {
    RoomStatus.available => 'Available',
    RoomStatus.occupied => 'Occupied',
    RoomStatus.cleaning => 'Cleaning',
    RoomStatus.maintenance => 'Maintenance',
  };
}

// Widget is now simple
Text(room.status.label)
```

---

## Agent Verification Checklist

- [ ] Characterization tests exist and are **green** before the first refactor change.
- [ ] Refactor is in a separate PR/commit from any feature additions.
- [ ] Each step of the refactor leaves all tests green (no "broken middle" commits).
- [ ] `flutter analyze` is run after every step — no new warnings introduced.
- [ ] Large refactors (affecting multiple features) have an accepted ADR.
- [ ] Coverage did not drop below thresholds after the refactor.
- [ ] No new external packages introduced during a refactor without an ADR.
- [ ] Fat Notifier refactors move business logic to UseCases — not just different Notifiers.
- [ ] Widget logic refactors move display logic to entity extensions or view models.
- [ ] Refactor PR description explains what structural change was made and why.
