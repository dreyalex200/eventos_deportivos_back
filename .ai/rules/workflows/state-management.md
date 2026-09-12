# State Management Workflow
**Related**: [state-management.md](../mobile/state-management.md) | [riverpod.md](../mobile/riverpod.md) | [screen-development.md](./screen-development.md)

---

## Purpose

Define the decision process for introducing any new piece of state. This workflow prevents over-engineering (putting everything in Riverpod) and under-engineering (losing state that should persist).

---

## Decision Tree

```
New state needed
       │
       ▼
Is it controller-based? (Animation, Text, Scroll, Focus)
├── YES → StatefulWidget / ConsumerStatefulWidget local field
└── NO ──▶ Is it needed by more than one widget?
            ├── NO ──▶ Is it ephemeral UI state? (toggle, hover, selected item in list)
            │          ├── YES → @riverpod Notifier (autoDispose) on feature scope
            │          └── NO  → StatefulWidget local field
            └── YES ──▶ Does it survive navigation (session-level)?
                        ├── YES → @Riverpod(keepAlive: true) Notifier
                        └── NO  → @riverpod Notifier (autoDispose)
                                  └── Does it persist across app restarts?
                                      ├── YES → + save to PreferencesService/SecureStorage
                                      └── NO  → autoDispose only
```

---

## Step 1: Classify the State

Fill out this classification before writing any code:

| Question | Answer |
|---|---|
| What data does this state hold? | |
| Who needs it? (1 widget / feature / global) | |
| How long must it live? (widget / screen / session / forever) | |
| Does it derive from other state? | |
| Does it need to persist? | |

---

## Step 2: Choose the Provider Type

Based on the classification, select the correct Riverpod type:

```dart
// CASE 1: Synchronous, mutable, feature-scoped
@riverpod
class RoomFiltersNotifier extends _$RoomFiltersNotifier {
  @override
  RoomFilters build() => const RoomFilters();
  // ... update methods
}

// CASE 2: Async, mutable, feature-scoped
@riverpod
class BookingDetailNotifier extends _$BookingDetailNotifier {
  @override
  Future<BookingDetailState> build(String bookingId) async { ... }
}

// CASE 3: Reactive stream from local DB
@riverpod
class RoomListNotifier extends _$RoomListNotifier {
  @override
  Stream<List<Room>> build() => ref.watch(roomRepositoryProvider).watchRooms()
      .map((e) => e.fold((f) => throw f, (rooms) => rooms));
}

// CASE 4: Global, session-persistent
@Riverpod(keepAlive: true)
class AuthNotifier extends _$AuthNotifier { ... }

// CASE 5: Derived/computed — no Notifier needed
@riverpod
Future<List<Room>> filteredRooms(Ref ref) async {
  final rooms = await ref.watch(roomListProvider.future);
  final filters = ref.watch(roomFiltersNotifierProvider);
  return filters.apply(rooms);
}
```

---

## Step 3: Data Transformation Rules

When state is derived from other state, apply transformations in a `Provider`, not in the widget:

```dart
// ✅ CORRECT: Transformation in a derived provider
@riverpod
Future<List<Room>> availableRooms(Ref ref) async {
  final rooms = await ref.watch(roomListProvider.future);
  return rooms.where((r) => r.status == RoomStatus.available).toList();
}

// Widget simply watches the derived provider
final available = ref.watch(availableRoomsProvider);

// ❌ WRONG: Transformation in widget build()
final rooms = ref.watch(roomListProvider).valueOrNull ?? [];
final available = rooms.where((r) => r.status == RoomStatus.available).toList(); // ❌ Runs on every rebuild
```

---

## Step 4: Register the Provider

Place providers in the correct file based on their scope:

| Scope | File Location |
|---|---|
| Infrastructure | `lib/core/di/providers.dart` |
| Feature DataSource | `lib/features/{x}/data/datasources/{x}_data_source.dart` |
| Feature Repository | `lib/features/{x}/data/repositories/{x}_repository_impl.dart` |
| Feature UseCase | `lib/features/{x}/domain/usecases/{x}_usecase.dart` |
| Feature Notifier | `lib/features/{x}/presentation/providers/{x}_notifier.dart` |
| Feature derived | `lib/features/{x}/presentation/providers/{x}_providers.dart` |
| Global state | `lib/core/di/providers.dart` |

---

## Step 5: Connect to UI

```dart
// In a ConsumerWidget:
// ✅ CORRECT: watch for reactive binding, read for actions
final state = ref.watch(myNotifierProvider);
final notifier = ref.read(myNotifierProvider.notifier);

// ✅ CORRECT: select for partial updates
final isLoading = ref.watch(myNotifierProvider.select((s) => s.isLoading));

// ✅ CORRECT: listen for side effects
ref.listen(myNotifierProvider, (_, next) {
  if (next case AsyncData(:final value) when value.isComplete) {
    context.pop();
  }
});
```

---

## Agent Verification Checklist

- [ ] State classification is documented (even if only as a comment) before implementation.
- [ ] `AnimationController`, `TextEditingController` are in `StatefulWidget` — never in Riverpod.
- [ ] Global state providers are `@Riverpod(keepAlive: true)`. Feature state is `@riverpod` (autoDispose).
- [ ] Derived/computed state is in a separate `@riverpod` provider, not computed in `build()`.
- [ ] Persisted state (theme, locale) saves to `PreferencesService` on every state change.
- [ ] All providers are placed in their correct file location per the scope table above.
- [ ] `ref.watch` used for reactivity in `build()`. `ref.read` used in action methods.
- [ ] `ref.listen` used for all navigation/snackbar side effects triggered by state changes.
- [ ] Stream-based state (`watchX`) uses `StreamNotifier` or `StreamProvider` — not `FutureProvider`.
- [ ] No provider holds state that belongs in the domain entity (business data lives in entities, not providers).
