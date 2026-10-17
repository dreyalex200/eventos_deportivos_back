package com.drey.eventsSports.application.usecases;

import com.drey.eventsSports.domain.model.entities.SportEvent;
import com.drey.eventsSports.domain.model.exceptions.EventNotFoundException;
import com.drey.eventsSports.domain.ports.inbound.*;
import com.drey.eventsSports.domain.ports.outbound.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EventUseCaseImpl implements GetEventsUseCase, GetEventByIdUseCase, CreateEventUseCase, UpdateEventUseCase, DeleteEventUseCase {

    private final EventRepository eventRepository;

    public EventUseCaseImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Page<SportEvent> getEvents(int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        return eventRepository.findAll(pageable);
    }

    @Override
    public SportEvent getEventById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
    }

    @Override
    public SportEvent createEvent(CreateEventCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateEventCommand cannot be null");
        }
        if (command.startDate() != null && command.endDate() != null && command.startDate().isAfter(command.endDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        SportEvent newEvent = SportEvent.builder()
                .title(command.title() != null ? command.title().trim() : "")
                .description(command.description() != null ? command.description().trim() : "")
                .sportType(command.sportType() != null ? command.sportType().trim().toUpperCase() : "FOOTBALL")
                .location(command.location() != null ? command.location().trim() : "")
                .startDate(command.startDate())
                .endDate(command.endDate())
                .status("DRAFT")
                .maxParticipants(command.maxParticipants() != null ? command.maxParticipants() : 0)
                .currentParticipants(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return eventRepository.save(newEvent);
    }

    @Override
    public SportEvent updateEvent(Long id, UpdateEventCommand command) {
        SportEvent existing = getEventById(id);

        LocalDateTime startDate = command.startDate() != null ? command.startDate() : existing.getStartDate();
        LocalDateTime endDate = command.endDate() != null ? command.endDate() : existing.getEndDate();
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        SportEvent updated = SportEvent.builder()
                .id(existing.getId())
                .title(command.title() != null ? command.title().trim() : existing.getTitle())
                .description(command.description() != null ? command.description().trim() : existing.getDescription())
                .sportType(command.sportType() != null ? command.sportType().trim().toUpperCase() : existing.getSportType())
                .location(command.location() != null ? command.location().trim() : existing.getLocation())
                .startDate(startDate)
                .endDate(endDate)
                .status(command.status() != null ? command.status().trim().toUpperCase() : existing.getStatus())
                .maxParticipants(command.maxParticipants() != null ? command.maxParticipants() : existing.getMaxParticipants())
                .currentParticipants(existing.getCurrentParticipants())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .deletedAt(existing.getDeletedAt())
                .build();

        return eventRepository.save(updated);
    }

    @Override
    public void deleteEvent(Long id) {
        SportEvent existing = getEventById(id);
        eventRepository.deleteById(existing.getId());
    }

    private Pageable createPageable(int page, int size, String sort) {
        if (size <= 0) {
            size = 10;
        }
        if (page < 0) {
            page = 0;
        }

        Sort sortObj = Sort.by(Sort.Direction.DESC, "startDate");
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            if ("startDate".equalsIgnoreCase(property) || "start_date".equalsIgnoreCase(property)) {
                property = "startDate";
            } else if ("id".equalsIgnoreCase(property)) {
                property = "id";
            } else if ("title".equalsIgnoreCase(property) || "name".equalsIgnoreCase(property)) {
                property = "name";
            }

            Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            sortObj = Sort.by(direction, property);
        }

        return PageRequest.of(page, size, sortObj);
    }
}
