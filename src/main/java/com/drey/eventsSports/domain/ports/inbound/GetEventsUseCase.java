package com.drey.eventsSports.domain.ports.inbound;

import com.drey.eventsSports.domain.model.entities.SportEvent;
import org.springframework.data.domain.Page;

public interface GetEventsUseCase {
    Page<SportEvent> getEvents(int page, int size, String sort);
}
