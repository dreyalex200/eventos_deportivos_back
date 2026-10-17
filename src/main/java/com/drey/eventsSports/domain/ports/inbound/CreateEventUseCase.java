package com.drey.eventsSports.domain.ports.inbound;

import com.drey.eventsSports.domain.model.entities.SportEvent;

public interface CreateEventUseCase {
    SportEvent createEvent(CreateEventCommand command);
}
