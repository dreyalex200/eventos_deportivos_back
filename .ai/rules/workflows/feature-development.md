# Feature Development Workflow
**Related**: [clean-architecture.md](../architecture/clean-architecture.md) | [folder-structure.md](../architecture/folder-structure.md) | [sdd.md](../architecture/sdd.md) | [environment-configuration.md](../../specs/infrastructure/environment-configuration.md)

---

## Purpose

Define the exact sequence of steps for implementing any new feature. This workflow ensures architectural integrity at every phase. No phase may be skipped.

---

## Full Feature Lifecycle

```
Phase 0: Spec Review
Phase 1: Domain — Entities, Failures, Repository Interface, Use Cases
Phase 2: Data — DTOs, DataSources, Repository Implementation
Phase 3: Presentation — State, Notifier, Screen, Widgets
Phase 4: Tests — Unit, Widget, Integration
Phase 5: Review & Merge
```

---

## Phase 0: Spec Review

Before writing any code:

1. Read the feature spec in `.ai/specs/features/{feature-name}.md`.
2. Check `.ai/specs/decisions/` for any accepted ADRs affecting this feature.
3. Review existing environment variables, identify new infrastructure requirements, update `.env.example`, and update environment documentation if necessary.
4. Verify the feature fits existing patterns. If it requires a new pattern → create ADR draft → **stop**.
5. Create the feature folder structure as defined in [folder-structure.md](../architecture/folder-structure.md).

```bash
# Create feature structure
mkdir -p lib/features/{feature_name}/{data/{datasources,dtos,repositories},domain/{entities,failures,repositories,usecases},presentation/{providers,screens,state,widgets}}
mkdir -p test/features/{feature_name}/{data/{datasources,repositories},domain/usecases,presentation/{providers,screens}}
```

---

## Phase 1: Domain Layer

**Order matters.** Build inner-out: Entities → Failures → Repository Interface → Use Cases.

### Step 1.1 — Define Entities

```dart
// lib/features/{feature}/domain/entities/{model}.dart
// Rules: final class, all final fields, copyWith, zero external imports
final class Room {
  const Room({
    required this.id,
    required this.number,
    required this.status,
    required this.pricePerNight,
    required this.createdAt,
    this.updatedAt,
  });

  final String id;
  final String number;
  final RoomStatus status;
  final double pricePerNight;
  final DateTime createdAt;
  final DateTime? updatedAt;

  Room copyWith({...}) => Room(...);
}

// Supporting enums also in domain
enum RoomStatus { available, occupied, cleaning, maintenance }
```

### Step 1.2 — Define Failures

```dart
// lib/features/{feature}/domain/failures/{feature}_failure.dart
// Extend or reuse the core Failure sealed class
// Only add feature-specific failures if core failures don't cover the case
```

### Step 1.3 — Define Repository Interface

```dart
// lib/features/{feature}/domain/repositories/{feature}_repository.dart
abstract interface class RoomRepository {
  Stream<Either<Failure, List<Room>>> watchRooms();
  Future<Either<Failure, Room>> getById(String id);
  Future<Either<Failure, Room>> create(Room room);
  Future<Either<Failure, Room>> update(Room room);
  Future<Either<Failure, void>> delete(String id);
  Future<Either<Failure, void>> syncRooms();
}
```

### Step 1.4 — Implement Use Cases

```dart
// One file per use case — one execute() method per class
// lib/features/{feature}/domain/usecases/get_room_usecase.dart
final class GetRoomUseCase {
  const GetRoomUseCase(this._repository);
  final RoomRepository _repository;

  Future<Either<Failure, Room>> execute(String roomId) async {
    if (roomId.trim().isEmpty) {
      return const Left(ValidationFailure(fields: {'roomId': 'Required'}));
    }
    return _repository.getById(roomId);
  }
}
```

### Phase 1 Gate ✅

- [ ] All entities are `final class` with `copyWith`.
- [ ] Repository interface is `abstract interface class`.
- [ ] Each use case has exactly one `execute()` method.
- [ ] Zero Flutter/infrastructure imports in domain.

---

## Phase 2: Data Layer

### Step 2.1 — Define DTOs

```dart
// lib/features/{feature}/data/dtos/{model}_dto.dart
@JsonSerializable()
final class RoomDto {
  const RoomDto({required this.id, required this.number, ...});

  factory RoomDto.fromJson(Map<String, dynamic> json) => _$RoomDtoFromJson(json);
  Map<String, dynamic> toJson() => _$RoomDtoToJson(this);

  Room toDomain() => Room(id: id, number: number, ...);
}

// Run code generation
// flutter pub run build_runner build --delete-conflicting-outputs
```

### Step 2.2 — Implement DataSources

```dart
// Remote: lib/features/{feature}/data/datasources/{feature}_remote_data_source_impl.dart
// Local: lib/features/{feature}/data/datasources/{feature}_local_data_source_impl.dart
// Each implements its corresponding abstract interface
```

### Step 2.3 — Implement Repository

```dart
// lib/features/{feature}/data/repositories/{feature}_repository_impl.dart
// Implements the domain interface
// Catches ALL exceptions, returns Left(Failure)
// Maps DTOs to Domain Entities at this boundary
```

### Step 2.4 — Wire Riverpod Providers

```dart
// lib/features/{feature}/data/datasources/{feature}_remote_data_source.dart (add provider)
@riverpod
RoomRemoteDataSource roomRemoteDataSource(Ref ref) =>
    RoomRemoteDataSourceImpl(ref.watch(dioProvider));

@riverpod
RoomRepository roomRepository(Ref ref) =>
    RoomRepositoryImpl(remote: ref.watch(roomRemoteDataSourceProvider), ...);

@riverpod
GetRoomUseCase getRoomUseCase(Ref ref) =>
    GetRoomUseCase(ref.watch(roomRepositoryProvider));
```

### Phase 2 Gate ✅

- [ ] Every DTO has `toDomain()`.
- [ ] Repository impl has `try/catch` on every method returning `Either`.
- [ ] Providers wired from infrastructure → DataSource → Repository → UseCase.

---

## Phase 3: Presentation Layer

### Step 3.1 — Define State

```dart
// lib/features/{feature}/presentation/state/{feature}_state.dart
sealed class RoomListState { const RoomListState(); }
final class RoomListInitial extends RoomListState { const RoomListInitial(); }
final class RoomListLoaded extends RoomListState {
  const RoomListLoaded({required this.rooms, this.filters = const RoomFilters()});
  final List<Room> rooms;
  final RoomFilters filters;
}
```

### Step 3.2 — Implement Notifier

```dart
// lib/features/{feature}/presentation/providers/{feature}_notifier.dart
@riverpod
class RoomListNotifier extends _$RoomListNotifier { ... }
```

### Step 3.3 — Build Screens and Widgets

Follow [screen-development.md](./screen-development.md) for screen-level guidelines.

### Phase 3 Gate ✅

- [ ] Notifiers call only UseCases — never DataSources.
- [ ] State is sealed and immutable.
- [ ] Widgets use design system components only.
- [ ] `ref.listen` used for navigation/snackbar side effects.

---

## Phase 4: Tests

Write tests in parallel with implementation — minimum coverage thresholds apply.

1. **UseCase tests**: all success, validation, and failure paths.
2. **RepositoryImpl tests**: remote success, remote fail+cache, both fail.
3. **Notifier tests**: all state transitions.
4. **Widget tests**: loading, data, error, and interaction states.

---

## Phase 5: Review & Merge

Checklist before PR:

- [ ] `flutter analyze` → zero errors/warnings.
- [ ] `dart run custom_lint` → zero import violations.
- [ ] `flutter test --coverage` → coverage above thresholds.
- [ ] All Phase gates (1–4) are checked.
- [ ] PR description references the feature spec.

---

## Agent Verification Checklist

- [ ] Feature was built inner-out: Domain first, Data second, Presentation third.
- [ ] No phase was skipped. Domain entities existed before Data DTOs.
- [ ] Repository interface is in Domain. Repository implementation is in Data.
- [ ] All providers follow the chain: UseCase → Repository → DataSource.
- [ ] Feature folder matches the template in `folder-structure.md` exactly.
- [ ] Code generation was run after adding `@JsonSerializable` or `@riverpod` annotations.
- [ ] No business logic exists in Notifiers — only UseCase calls and state transitions.
- [ ] Feature spec in `.ai/specs/features/` was read before implementation started.
- [ ] Any new package introduced has an accepted ADR.
- [ ] All `.env.example` templates and documentation have been updated if new variables were added.
- [ ] All four test categories (UseCase, Repository, Notifier, Widget) have test files.
