package com.drey.eventsSports.domain.ports.inbound;

import com.drey.eventsSports.domain.model.entities.SportEvent;

public interface UpdateEventUseCase {
    SportEvent updateEvent(Long id, UpdateEventCommand command);
}
