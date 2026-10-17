package com.drey.eventsSports.domain.ports.inbound;

import java.time.LocalDateTime;

public record UpdateEventCommand(
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
