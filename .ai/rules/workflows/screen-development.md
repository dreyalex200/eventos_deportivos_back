# Screen Development Pipeline
**Related**: [feature-development.md](./feature-development.md) | [riverpod.md](../mobile/riverpod.md) | [components.md](../ui/components.md) | [responsive-design.md](../mobile/responsive-design.md)

---

## Purpose

Define the exact pipeline for building a single screen from folder creation to final widget composition. Every screen in the application follows this identical sequence.

---

## Step 1: Create Files

```bash
# Feature: Room Detail
# Create screen and supporting files
touch lib/features/rooms/presentation/screens/room_detail_screen.dart
touch lib/features/rooms/presentation/state/room_detail_state.dart
touch lib/features/rooms/presentation/providers/room_detail_notifier.dart
touch lib/features/rooms/presentation/widgets/room_detail_header.dart
touch lib/features/rooms/presentation/widgets/room_detail_amenities.dart
touch test/features/rooms/presentation/screens/room_detail_screen_test.dart
touch test/features/rooms/presentation/providers/room_detail_notifier_test.dart
```

---

## Step 2: Define Screen State

State must be defined **before** the Notifier or Screen. It drives the entire design.

```dart
// lib/features/rooms/presentation/state/room_detail_state.dart

/// Represents the full UI state of the Room Detail screen.
final class RoomDetailState {
  const RoomDetailState({
    required this.room,
    this.isEditing = false,
    this.isSaving = false,
  });

  final Room room;
  final bool isEditing;
  final bool isSaving;

  RoomDetailState copyWith({Room? room, bool? isEditing, bool? isSaving}) =>
      RoomDetailState(
        room: room ?? this.room,
        isEditing: isEditing ?? this.isEditing,
        isSaving: isSaving ?? this.isSaving,
      );
}
```

---

## Step 3: Implement the Notifier

```dart
// lib/features/rooms/presentation/providers/room_detail_notifier.dart
@riverpod
class RoomDetailNotifier extends _$RoomDetailNotifier {
  @override
  Future<RoomDetailState> build(String roomId) async {
    final result = await ref.watch(getRoomUseCaseProvider).execute(roomId);
    return result.fold(
      (failure) => throw failure,
      (room) => RoomDetailState(room: room),
    );
  }

  void toggleEditMode() {
    state = state.whenData(
      (s) => s.copyWith(isEditing: !s.isEditing),
    );
  }

  Future<void> saveChanges(UpdateRoomParams params) async {
    final current = state.valueOrNull;
    if (current == null) return;

    state = AsyncData(current.copyWith(isSaving: true));

    final result = await ref.read(updateRoomUseCaseProvider).execute(params);

    state = result.fold(
      (failure) {
        ref.read(snackbarNotifierProvider.notifier).showError(failure.message);
        return AsyncData(current.copyWith(isSaving: false));
      },
      (updatedRoom) => AsyncData(
        current.copyWith(room: updatedRoom, isSaving: false, isEditing: false),
      ),
    );
  }
}
```

---

## Step 4: Build the Screen

### Naming Convention

| Type | Pattern | Example |
|---|---|---|
| List screen | `{Feature}ListScreen` | `RoomListScreen` |
| Detail screen | `{Feature}DetailScreen` | `RoomDetailScreen` |
| Create screen | `{Feature}CreateScreen` | `RoomCreateScreen` |
| Edit screen | (use detail with edit mode) | `RoomDetailScreen(isEditing: true)` |
| Settings screen | `{Feature}SettingsScreen` | `ProfileSettingsScreen` |

### Screen Template

```dart
// lib/features/rooms/presentation/screens/room_detail_screen.dart
class RoomDetailScreen extends ConsumerWidget {
  const RoomDetailScreen({super.key, required this.roomId});
  final String roomId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(roomDetailNotifierProvider(roomId));
    final notifier = ref.read(roomDetailNotifierProvider(roomId).notifier);

    // Side effects (navigation, snackbar) via ref.listen — never in build
    ref.listen<AsyncValue<RoomDetailState>>(
      roomDetailNotifierProvider(roomId),
      (_, next) {
        // Handle specific transitions if needed
      },
    );

    return AppScaffold(
      title: 'Room Details',
      actions: [
        if (state.valueOrNull?.isEditing == false)
          AppIconButton(
            icon: Icons.edit_rounded,
            tooltip: 'Edit room',
            onPressed: notifier.toggleEditMode,
          ),
      ],
      body: switch (state) {
        AsyncLoading() => const AppLoadingIndicator(),
        AsyncError(:final error) => AppErrorWidget(
            message: error.toString(),
            onRetry: () => ref.invalidate(roomDetailNotifierProvider(roomId)),
          ),
        AsyncData(:final value) => _RoomDetailContent(
            state: value,
            notifier: notifier,
          ),
        _ => const SizedBox.shrink(),
      },
    );
  }
}
```

---

## Step 5: Decompose into Sub-Widgets

**Rule**: If a widget builds more than ~50 lines, decompose it into named sub-widgets (separate classes, not methods).

```dart
// ✅ CORRECT: Separate classes for each section
class _RoomDetailContent extends StatelessWidget {
  const _RoomDetailContent({required this.state, required this.notifier});
  final RoomDetailState state;
  final RoomDetailNotifier notifier;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: AppSpacing.screenPadding,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          _RoomHeader(room: state.room),
          const SizedBox(height: AppSpacing.xl),
          _RoomStatusSection(room: state.room, isEditing: state.isEditing),
          const SizedBox(height: AppSpacing.xl),
          _RoomPricingSection(room: state.room, isEditing: state.isEditing),
          const SizedBox(height: AppSpacing.xl),
          if (state.isEditing)
            AppButton.filled(
              label: 'Save Changes',
              isLoading: state.isSaving,
              onPressed: () => notifier.saveChanges(_buildParams()),
            ),
        ],
      ),
    );
  }

  UpdateRoomParams _buildParams() => UpdateRoomParams(id: state.room.id, ...);
}

class _RoomHeader extends StatelessWidget {
  const _RoomHeader({required this.room});
  final Room room;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Text(
            'Room ${room.number}',
            style: Theme.of(context).textTheme.headlineSmall,
          ),
        ),
        AppBadge(
          label: room.status.label,
          variant: room.status.badgeVariant,
        ),
      ],
    );
  }
}
```

---

## Step 6: Responsive Layout

All screens must handle phone and tablet layouts. See [responsive-design.md](../mobile/responsive-design.md).

```dart
@override
Widget build(BuildContext context, WidgetRef ref) {
  // ... state logic

  return AppScaffold(
    body: ResponsiveLayout(
      phone: _RoomDetailPhoneLayout(state: value),
      tablet: _RoomDetailTabletLayout(state: value),
    ),
  );
}
```

---

## Step 7: Accessibility Pass

Before marking the screen complete:

- [ ] All interactive elements have 48dp min touch target.
- [ ] All images have `semanticLabel`.
- [ ] Custom interactive widgets have `Semantics` wrapper.
- [ ] Focus order is logical (test with keyboard navigation).

---

## Agent Verification Checklist

- [ ] State class is defined before Notifier or Screen are implemented.
- [ ] Screen class is a `ConsumerWidget`. Never `StatelessWidget` for screens needing provider access.
- [ ] `ref.listen` is used for all side effects. No side effects in `build()` body.
- [ ] `switch (state) { AsyncLoading, AsyncError, AsyncData }` covers all three cases in the screen.
- [ ] Sub-widgets are separate named classes (not private methods). No `_buildX()` methods.
- [ ] Screen name follows the convention: `{Feature}ListScreen`, `{Feature}DetailScreen`, `{Feature}CreateScreen`.
- [ ] `AppScaffold` is used as the root widget (not raw `Scaffold`).
- [ ] Retry action calls `ref.invalidate(notifierProvider(...))`.
- [ ] Tablet layout is handled via `ResponsiveLayout` or `context.isTablet` check.
- [ ] A corresponding `_test.dart` file exists for both the screen and the notifier.
