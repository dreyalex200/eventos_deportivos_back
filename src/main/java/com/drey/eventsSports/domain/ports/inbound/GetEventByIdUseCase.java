package com.drey.eventsSports.domain.ports.inbound;

import com.drey.eventsSports.domain.model.entities.SportEvent;

public interface GetEventByIdUseCase {
    SportEvent getEventById(Long id);
}
