# Bug Fix Protocol
**Related**: [testing.md](../mobile/testing.md) | [constitution.md](../constitution.md) | [refactor.md](./refactor.md)

---

## Purpose

Define the mandatory debugging and bug fix protocol. Fixes without tests are not fixes — they are temporary patches that will regress. Every bug fix follows the Reproduce → Test → Fix → Verify sequence.

---

## The Five-Step Protocol

```
Step 1: Reproduce
Step 2: Write a Failing Test (Red)
Step 3: Root Cause Analysis
Step 4: Fix (Green)
Step 5: Regression Check
```

---

## Step 1: Reproduce

Before any code change, reliably reproduce the bug.

```
1. Document: What is the expected behavior?
2. Document: What is the actual (broken) behavior?
3. Document: What are the exact reproduction steps?
4. Identify: Which layer is the bug in? (Presentation / Domain / Data / Infrastructure)
5. Isolate: Can it be reproduced in a unit test? Or only in a widget/integration test?
```

**Bug Report Template** (add as a comment in the test file):

```dart
// BUG: When a guest checks out and immediately checks back in,
// the room status briefly shows "Available" before showing "Occupied".
//
// EXPECTED: Room status immediately shows "Occupied" on re-check-in.
// ACTUAL: Shows "Available" for ~500ms due to stale cache.
// REPRODUCTION:
//   1. Check out a guest from Room 101
//   2. Immediately check a new guest into Room 101
//   3. Observe the room card flicker
//
// ROOT CAUSE: RoomRepositoryImpl.syncRooms() is not invalidating cache
// before the new check-in data arrives.
//
// LAYER: Data — RoomRepositoryImpl
```

---

## Step 2: Write a Failing Test (Red)

Write the test **before** the fix. The test must fail against the current code.

```dart
// test/features/rooms/data/repositories/room_repository_impl_test.dart
test('room status is immediately Occupied after check-in, not Available', () async {
  // Arrange
  final staleRoom = Room(id: '1', number: '101', status: RoomStatus.available, ...);
  mockLocal.seedRoom(staleRoom); // Cache has stale "Available" status

  final updatedDto = RoomDto(id: '1', number: '101', status: 'occupied', ...);
  when(() => mockRemote.fetchRoom('1')).thenAnswer((_) async => updatedDto);

  // Act
  final result = await repository.getById('1');

  // Assert — MUST return "occupied", not "available" from stale cache
  expect(
    result.fold((f) => null, (r) => r.status),
    equals(RoomStatus.occupied), // ← This FAILS before the fix
  );
});
```

**Rule**: If you cannot write a failing test, the bug is not well enough understood. Continue analysis.

---

## Step 3: Root Cause Analysis

Identify the exact line causing the bug. Do not guess.

```
Techniques:
├── Add breakpoints / debugPrint at layer boundaries
├── Check the data flow: API → DTO → Repository → UseCase → Notifier → Widget
├── Isolate each step with a unit test to find which layer returns wrong data
├── Check timing: Is it a race condition? Use async/await carefully.
└── Check state: Is a provider not invalidating when it should?
```

**Common Root Cause Patterns**:

| Symptom | Common Root Cause |
|---|---|
| Stale data after an action | Provider not invalidated after mutation |
| Wrong data type | Missing DTO field mapping or wrong JSON key |
| State lost on navigation | `autoDispose` provider disposed too early — consider `keepAlive` |
| Error not shown to user | `Either` `Left` case not handled in Notifier |
| Double API call | `ref.watch` in action method instead of `ref.read` |
| Null crash | Missing null check on optional field from API |

---

## Step 4: Fix (Green)

Apply the minimal fix that makes the failing test pass. Do not refactor or add features during a bug fix.

```dart
// ✅ CORRECT fix: Invalidate stale cache before returning remote data
@override
Future<Either<Failure, Room>> getById(String id) async {
  try {
    final dto = await remote.fetchRoom(id);
    final entity = dto.toDomain();
    // FIX: Clear stale cache entry BEFORE writing new data
    await local.deleteRoom(id);
    await local.upsertRoom(entity);
    return Right(entity);
  } on DioException catch (e) {
    final cached = await local.getCachedRoom(id);
    if (cached != null) return Right(cached);
    return Left(ApiExceptionMapper.fromDioException(e));
  }
}
```

**Rules**:
- Minimum change principle: fix only what's broken.
- If the fix requires restructuring — schedule a refactor separately.
- If the fix reveals a deeper architectural problem — create an ADR.

---

## Step 5: Regression Check

```bash
# 1. Run the specific test to confirm it passes
flutter test test/features/rooms/data/repositories/room_repository_impl_test.dart

# 2. Run the full test suite for the affected feature
flutter test test/features/rooms/

# 3. Run the full test suite
flutter test

# 4. Run static analysis
flutter analyze

# 5. Check coverage didn't drop
flutter test --coverage
```

**Regression Check Rules**:
- Zero new test failures permitted.
- Coverage must not drop below established thresholds.
- `flutter analyze` must return zero errors.

---

## Architecture Violation Bugs

If the bug exists because of an architecture violation (e.g., business logic in a Widget, Notifier accessing a DataSource directly), the fix **must** correct the architecture — not work around it.

```dart
// BUG: Null crash in RoomListScreen because Widget does business logic
// ❌ WRONG fix: Add null check in widget
Text(room.price != null ? '\$${room.price}' : 'N/A') // Hides real problem

// ✅ CORRECT fix: Move logic to Domain entity
// In Room entity:
String get formattedPrice => '\$${pricePerNight.toStringAsFixed(2)}/night';

// In Widget (just display):
Text(room.formattedPrice)
```

---

## Agent Verification Checklist

- [ ] A failing test exists that reproduces the bug **before** the fix is applied.
- [ ] The test passes after the fix and continues to pass on subsequent runs.
- [ ] Root cause is documented in the test file as a comment.
- [ ] The fix is the minimum change needed. No scope creep into refactoring.
- [ ] If the bug is in the wrong layer (e.g., business logic in Widget), the fix moves it to the correct layer.
- [ ] Full test suite runs without regressions after the fix.
- [ ] `flutter analyze` returns zero errors after the fix.
- [ ] If the bug reveals an architectural problem, an ADR is created before the architectural fix.
- [ ] The fixed test is added to the permanent test suite (not deleted after the bug is fixed).
- [ ] Coverage does not drop below thresholds after the fix.
