# API Integration Workflow
**Related**: [feature-development.md](./feature-development.md) | [clean-architecture.md](../architecture/clean-architecture.md) | [environment-configuration.md](../../specs/infrastructure/environment-configuration.md)

---

## Purpose

Define the exact sequence for integrating a new API endpoint into the application. Every endpoint follows the same four-step pattern without exception.

---

## The Five-Step Pattern

```
Step 0: Environment Configuration
Step 1: Define DTO
Step 2: Implement API client method (DataSource)
Step 3: Map to Domain (Repository)
Step 4: Consume in Provider (Notifier)
```

---

## Step 0: Environment Configuration

Before defining any code, identify and document all configuration variables required by the new integration.

1. **Endpoint URLs**: Base API URLs.
2. **Credentials**: API keys, OAuth secrets, tokens.
3. **Timeouts**: Read, write, and connect timeouts.
4. **Retry Policies**: Max retries and backoff limits.

Update the `.env.example` template with these variables using `<CHANGE_ME>` placeholders for sensitive credentials, and update the environment documentation correspondingly.

---

## Step 1: Define the DTO

Before touching any other layer, define the DTO that maps the API response shape.

```dart
// lib/features/rooms/data/dtos/room_dto.dart
import 'package:json_annotation/json_annotation.dart';
import '../../domain/entities/room.dart';

part 'room_dto.g.dart';

@JsonSerializable(explicitToJson: true)
final class RoomDto {
  const RoomDto({
    required this.id,
    required this.number,
    required this.type,
    required this.status,
    required this.pricePerNight,
    required this.createdAt,
    this.updatedAt,
    this.amenities = const [],
  });

  final String id;
  final String number;
  final String type;
  final String status;
  @JsonKey(name: 'price_per_night')
  final double pricePerNight;
  @JsonKey(name: 'created_at')
  final DateTime createdAt;
  @JsonKey(name: 'updated_at')
  final DateTime? updatedAt;
  final List<String> amenities;

  factory RoomDto.fromJson(Map<String, dynamic> json) =>
      _$RoomDtoFromJson(json);
  Map<String, dynamic> toJson() => _$RoomDtoToJson(this);

  /// Boundary mapping: DTO → Domain Entity
  Room toDomain() => Room(
    id: id,
    number: number,
    type: RoomType.fromString(type),
    status: RoomStatus.fromString(status),
    pricePerNight: pricePerNight,
    amenities: amenities,
    createdAt: createdAt,
    updatedAt: updatedAt,
  );
}

// Request DTOs (for POST/PUT bodies)
@JsonSerializable()
final class CreateRoomRequestDto {
  const CreateRoomRequestDto({
    required this.number,
    required this.type,
    required this.pricePerNight,
  });

  final String number;
  final String type;
  @JsonKey(name: 'price_per_night')
  final double pricePerNight;

  Map<String, dynamic> toJson() => _$CreateRoomRequestDtoToJson(this);
}

// Run code gen after defining DTOs:
// flutter pub run build_runner build --delete-conflicting-outputs
```

**Gate**: DTO mirrors the exact API response shape. `toDomain()` is implemented. Code gen runs without errors.

---

## Step 2: Implement API Client Method (DataSource)

Add the method to the abstract interface first, then to the implementation.

```dart
// Abstract interface — lib/features/rooms/data/datasources/room_remote_data_source.dart
abstract interface class RoomRemoteDataSource {
  Future<List<RoomDto>> fetchAllRooms({RoomStatus? status, int? page});
  Future<RoomDto> fetchRoom(String id);
  Future<RoomDto> createRoom(CreateRoomRequestDto request);
  Future<RoomDto> updateRoom(String id, UpdateRoomRequestDto request);
  Future<void> deleteRoom(String id);
}

// Implementation — lib/features/rooms/data/datasources/room_remote_data_source_impl.dart
final class RoomRemoteDataSourceImpl implements RoomRemoteDataSource {
  const RoomRemoteDataSourceImpl(this._dio);
  final Dio _dio;

  @override
  Future<List<RoomDto>> fetchAllRooms({RoomStatus? status, int? page}) async {
    final response = await _dio.get<List<dynamic>>(
      ApiEndpoints.rooms,
      queryParameters: {
        if (status != null) 'status': status.name,
        if (page != null) 'page': page,
      },
    );
    return (response.data ?? [])
        .map((json) => RoomDto.fromJson(json as Map<String, dynamic>))
        .toList();
  }

  @override
  Future<RoomDto> createRoom(CreateRoomRequestDto request) async {
    final response = await _dio.post<Map<String, dynamic>>(
      ApiEndpoints.rooms,
      data: request.toJson(),
    );
    return RoomDto.fromJson(response.data!);
  }

  @override
  Future<void> deleteRoom(String id) async {
    await _dio.delete(ApiEndpoints.roomById(id));
  }

  // ... other methods
}
```

**Gate**: DataSource returns DTOs only. No Domain entities or raw `dynamic` maps escape the DataSource.

---

## Step 3: Map to Domain (Repository)

Add the corresponding method to the repository implementation. It calls the DataSource, maps DTOs to entities, and wraps everything in `Either<Failure, T>`.

```dart
// lib/features/rooms/data/repositories/room_repository_impl.dart
final class RoomRepositoryImpl implements RoomRepository {
  const RoomRepositoryImpl({
    required this.remote,
    required this.local,
  });

  final RoomRemoteDataSource remote;
  final RoomLocalDataSource local;

  @override
  Future<Either<Failure, List<Room>>> getRooms({RoomStatus? status}) async {
    try {
      // 1. Fetch DTOs from remote
      final dtos = await remote.fetchAllRooms(status: status);
      // 2. Map to domain entities at the boundary
      final entities = dtos.map((dto) => dto.toDomain()).toList();
      // 3. Cache locally
      await local.upsertAllRooms(entities);
      // 4. Return domain entities wrapped in Right
      return Right(entities);
    } on DioException catch (e) {
      // 5. On failure, try cache
      final cached = await local.getAllRooms();
      if (cached.isNotEmpty) return Right(cached);
      return Left(ApiExceptionMapper.fromDioException(e));
    } catch (e) {
      return Left(NetworkFailure(message: e.toString()));
    }
  }

  @override
  Future<Either<Failure, Room>> createRoom(Room room) async {
    try {
      final requestDto = CreateRoomRequestDto(
        number: room.number,
        type: room.type.name,
        pricePerNight: room.pricePerNight,
      );
      final responseDto = await remote.createRoom(requestDto);
      final entity = responseDto.toDomain();
      await local.upsertRoom(entity);
      return Right(entity);
    } on DioException catch (e) {
      return Left(ApiExceptionMapper.fromDioException(e));
    }
  }
}
```

**Gate**: Every repository method returns `Either<Failure, T>`. No raw exceptions escape. DTOs are never passed to calling code.

---

## Step 4: Consume in Provider (Notifier)

Add the use case call and state update to the Notifier. The Notifier never calls the Repository directly.

```dart
// lib/features/rooms/presentation/providers/room_list_notifier.dart
@riverpod
class RoomListNotifier extends _$RoomListNotifier {
  @override
  Future<List<Room>> build() async {
    final result = await ref.watch(getAllRoomsUseCaseProvider).execute();
    return result.fold((f) => throw f, (rooms) => rooms);
  }

  Future<void> createRoom(CreateRoomParams params) async {
    // Optimistic update: show new room immediately
    final current = state.valueOrNull ?? [];
    state = AsyncData([...current, params.toOptimisticRoom()]);

    final result = await ref.read(createRoomUseCaseProvider).execute(params);

    result.fold(
      (failure) {
        // Rollback on failure
        state = AsyncData(current);
        ref.read(snackbarNotifierProvider.notifier)
            .showError('Failed to create room: ${failure.message}');
      },
      (room) {
        // Replace optimistic entry with real data
        state = AsyncData([
          ...current,
          room, // Real entity from server
        ]);
      },
    );
  }
}
```

**Gate**: Notifier calls UseCase only. No Dio, no DTO, no direct repository access.

---

## Adding Endpoint to Existing Feature Checklist

When adding a new endpoint to an existing feature (not a new feature):

- [ ] New configuration variables (URLs, credentials, timeouts) added to `.env.example`.
- [ ] New DTO class defined in `data/dtos/` with `fromJson`, `toJson`, and `toDomain()`.
- [ ] New endpoint constant added to `ApiEndpoints`.
- [ ] Method added to the DataSource interface first, then the implementation.
- [ ] Method added to the Repository interface first, then the implementation.
- [ ] New UseCase created in `domain/usecases/` (if new business operation).
- [ ] UseCase provider registered via `@riverpod`.
- [ ] Notifier updated with new action method.
- [ ] `build_runner` run after DTO changes.

---

## Agent Verification Checklist

- [ ] Step 1 (DTO) completes before Step 2 (DataSource). No DataSource method without a DTO return type.
- [ ] All `@JsonKey(name: 'snake_case')` annotations match the actual API response field names.
- [ ] `toDomain()` is defined on every response DTO. Request DTOs only have `toJson()`.
- [ ] DataSource methods return DTOs — never `Map<String, dynamic>` or Domain entities.
- [ ] Repository methods return `Either<Failure, Entity>` — never `Either<Failure, DTO>`.
- [ ] Every `try` block in the repository has `on DioException catch (e)` and a generic `catch (e)`.
- [ ] `ApiExceptionMapper.fromDioException(e)` is used — no custom exception-to-failure mapping inline.
- [ ] Notifier calls UseCase — never the Repository or DataSource.
- [ ] New `ApiEndpoints` constant added for every new endpoint path.
- [ ] `flutter pub run build_runner build` runs successfully after DTO changes.
