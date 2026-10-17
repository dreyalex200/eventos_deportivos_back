package com.drey.eventsSports.application.dtos;

import java.util.List;

public record EventPageResponse(
        List<EventResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
