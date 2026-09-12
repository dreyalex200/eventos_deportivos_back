# TDD Workflow
**Related**: [testing.md](../mobile/testing.md) | [feature-development.md](./feature-development.md) | [bug-fix.md](./bug-fix.md)

---

## Purpose

Define the Test-Driven Development cycle for this project. TDD is the default approach for all Domain and Data layer code. Presentation layer follows test-parallel development (tests written alongside implementation).

---

## Red → Green → Refactor Cycle

```
        ┌─────────────────────────────────────────────────┐
        │                                                 │
        ▼                                                 │
     🔴 RED                                               │
  Write a failing test                                    │
  (test that describes desired behavior)                  │
        │                                                 │
        ▼                                                 │
     🟢 GREEN                                             │
  Write the minimum code to make the test pass            │
  (no more, no less)                                      │
        │                                                 │
        ▼                                                 │
  🔵 REFACTOR                                             │
  Improve code structure without changing behavior        │
  (all tests must still pass)                             │
        │                                                 │
        └─────────────────────────────────────────────────┘
                    Repeat for next behavior
```

---

## TDD for Use Cases (Domain Layer)

Use Cases are the primary TDD target. Write all tests before implementing the class.

### Phase 1 — RED: Write All Test Cases First

```dart
// test/features/rooms/domain/usecases/create_room_usecase_test.dart

void main() {
  late CreateRoomUseCase useCase;
  late MockRoomRepository mockRepo;

  setUp(() {
    mockRepo = MockRoomRepository();
    useCase = CreateRoomUseCase(mockRepo);
  });

  group('CreateRoomUseCase', () {
    final tParams = CreateRoomParams(
      number: '101',
      type: RoomType.standard,
      pricePerNight: 150.0,
    );

    // Test 1: Happy path
    test('✅ returns Room when repository creates successfully', () async {
      when(() => mockRepo.create(any())).thenAnswer((_) async =>
          Right(Room(id: 'new-id', number: '101', ...)));

      final result = await useCase.execute(tParams);

      expect(result, isA<Right<Failure, Room>>());
    });

    // Test 2: Validation — empty number
    test('❌ returns ValidationFailure when room number is empty', () async {
      final result = await useCase.execute(tParams.copyWith(number: ''));

      expect(result.fold((f) => f, (_) => null), isA<ValidationFailure>());
      verifyNever(() => mockRepo.create(any())); // Repository never called
    });

    // Test 3: Validation — invalid price
    test('❌ returns ValidationFailure when price is negative', () async {
      final result = await useCase.execute(tParams.copyWith(pricePerNight: -1));

      expect(result.fold((f) => f, (_) => null), isA<ValidationFailure>());
    });

    // Test 4: Repository failure propagation
    test('❌ propagates NetworkFailure from repository', () async {
      when(() => mockRepo.create(any())).thenAnswer((_) async =>
          const Left(NetworkFailure(message: 'Server error')));

      final result = await useCase.execute(tParams);

      expect(result, isA<Left<Failure, Room>>());
      expect(result.fold((f) => f, (_) => null), isA<NetworkFailure>());
    });
  });
}
```

**Run tests: all 4 FAIL** (UseCase doesn't exist yet) ✅ Confirms RED.

---

### Phase 2 — GREEN: Implement Minimum Code

```dart
// lib/features/rooms/domain/usecases/create_room_usecase.dart
final class CreateRoomUseCase {
  const CreateRoomUseCase(this._repository);
  final RoomRepository _repository;

  Future<Either<Failure, Room>> execute(CreateRoomParams params) async {
    // Minimum validation to make tests pass
    if (params.number.trim().isEmpty) {
      return const Left(ValidationFailure(fields: {'number': 'Required'}));
    }
    if (params.pricePerNight < 0) {
      return const Left(ValidationFailure(fields: {'pricePerNight': 'Must be positive'}));
    }

    final room = Room(
      id: '', // Will be assigned by server
      number: params.number.trim(),
      type: params.type,
      pricePerNight: params.pricePerNight,
      status: RoomStatus.available,
      createdAt: DateTime.now(),
    );

    return _repository.create(room);
  }
}
```

**Run tests: all 4 PASS** ✅ Confirms GREEN.

---

### Phase 3 — REFACTOR: Improve Without Breaking

```dart
// Extract validation logic to a dedicated method
Future<Either<Failure, Room>> execute(CreateRoomParams params) async {
  final validationResult = _validate(params);
  if (validationResult != null) return Left(validationResult);
  return _repository.create(params.toEntity());
}

ValidationFailure? _validate(CreateRoomParams params) {
  final errors = <String, String>{};
  if (params.number.trim().isEmpty) errors['number'] = 'Required';
  if (params.pricePerNight < 0) errors['pricePerNight'] = 'Must be positive';
  if (params.pricePerNight > 10000) errors['pricePerNight'] = 'Exceeds maximum';
  return errors.isEmpty ? null : ValidationFailure(fields: errors);
}
```

**Run tests: all 4 still PASS** ✅ Confirms REFACTOR was safe.

---

## TDD for Repository Implementations (Data Layer)

```dart
// Write contract tests — all interface methods must be tested

void main() {
  // For each public method: happy path, partial failure, total failure
  group('RoomRepositoryImpl.create', () {
    test('creates and caches room on success', () async { ... });
    test('returns NetworkFailure on DioException', () async { ... });
    test('returns ValidationFailure on HTTP 422', () async { ... });
    test('returns ConflictFailure on HTTP 409 (room number exists)', () async { ... });
  });
}
```

---

## Test-Parallel Development (Presentation Layer)

For Notifiers and Widgets, write tests in parallel with implementation:

```
Implement Notifier.build()  →  Write test for build() state
Implement Notifier.action() →  Write test for action() state transition
Implement Screen widget     →  Write widget test for loading/data/error states
```

---

## Minimum Test Cases Per Unit

| Unit | Minimum Tests |
|---|---|
| UseCase | Happy path + each validation failure + each failure type from repo |
| RepositoryImpl method | Remote success + remote fail (with cache) + remote fail (no cache) |
| Notifier.build() | AsyncLoading → AsyncData + AsyncLoading → AsyncError |
| Notifier.action() | Success state transition + failure state transition |
| Widget | Loading state + Data state + Error state + Primary interaction |

---

## Agent Verification Checklist

- [ ] Tests are written (and failing) before the implementation code exists.
- [ ] Each RED phase produces tests that fail for the right reason (not compilation errors).
- [ ] Each GREEN phase uses minimum code — no over-engineering "just in case".
- [ ] Each REFACTOR phase runs the full test suite and confirms all tests still pass.
- [ ] Every UseCase has tests for: happy path, each validation error, each failure type.
- [ ] Every RepositoryImpl method has tests for: success + cache, remote failure + cache hit, both fail.
- [ ] Presentation layer tests are written in parallel (not after) implementation.
- [ ] `verifyNever(() => mockRepo.method(any()))` is used to confirm repository is not called on validation failure.
- [ ] Test file names follow `{source}_test.dart` convention.
- [ ] All tests use `setUp()` to initialize fresh mocks and instances.
