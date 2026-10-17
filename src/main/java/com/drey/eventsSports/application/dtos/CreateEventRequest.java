package com.drey.eventsSports.application.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank(message = "Title is required")
        String title,
        String description,
        @NotBlank(message = "Sport type is required")
        String sportType,
        String location,
        @NotNull(message = "Start date is required")
        LocalDateTime startDate,
        @NotNull(message = "End date is required")
        LocalDateTime endDate,
        Integer maxParticipants
) {
}
