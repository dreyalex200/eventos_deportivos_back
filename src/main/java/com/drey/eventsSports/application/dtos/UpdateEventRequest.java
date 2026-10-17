package com.drey.eventsSports.application.dtos;

import java.time.LocalDateTime;

public record UpdateEventRequest(
        String title,
        String description,
        String sportType,
        String location,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer maxParticipants,
        String status
) {
}
