package com.drey.eventsSports.infrastructure.controllers.secured;

import com.drey.eventsSports.application.dtos.*;
import com.drey.eventsSports.domain.model.entities.SportEvent;
import com.drey.eventsSports.domain.ports.inbound.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final GetEventsUseCase getEventsUseCase;
    private final GetEventByIdUseCase getEventByIdUseCase;
    private final CreateEventUseCase createEventUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final DeleteEventUseCase deleteEventUseCase;

    public EventController(
            GetEventsUseCase getEventsUseCase,
            GetEventByIdUseCase getEventByIdUseCase,
            CreateEventUseCase createEventUseCase,
            UpdateEventUseCase updateEventUseCase,
            DeleteEventUseCase deleteEventUseCase) {
        this.getEventsUseCase = getEventsUseCase;
        this.getEventByIdUseCase = getEventByIdUseCase;
        this.createEventUseCase = createEventUseCase;
        this.updateEventUseCase = updateEventUseCase;
        this.deleteEventUseCase = deleteEventUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<EventPageResponse>> getEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate,desc") String sort) {
        Page<SportEvent> eventPage = getEventsUseCase.getEvents(page, size, sort);

        List<EventResponse> items = eventPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        EventPageResponse pageResponse = new EventPageResponse(
                items,
                eventPage.getNumber(),
                eventPage.getSize(),
                eventPage.getTotalElements(),
                eventPage.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", pageResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        SportEvent event = getEventByIdUseCase.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success("Event retrieved successfully", toResponse(event)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody CreateEventRequest request) {
        CreateEventCommand command = new CreateEventCommand(
                request.title(),
                request.description(),
                request.sportType(),
                request.location(),
                request.startDate(),
                request.endDate(),
                request.maxParticipants()
        );

        SportEvent created = createEventUseCase.createEvent(command);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", toResponse(created)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @RequestBody UpdateEventRequest request) {
        UpdateEventCommand command = new UpdateEventCommand(
                request.title(),
                request.description(),
                request.sportType(),
                request.location(),
                request.startDate(),
                request.endDate(),
                request.maxParticipants(),
                request.status()
        );

        SportEvent updated = updateEventUseCase.updateEvent(id, command);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", toResponse(updated)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable Long id) {
        deleteEventUseCase.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }

    private EventResponse toResponse(SportEvent event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getSportType(),
                event.getLocation(),
                event.getStartDate(),
                event.getEndDate(),
                event.getStatus(),
                event.getMaxParticipants(),
                event.getCurrentParticipants(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
