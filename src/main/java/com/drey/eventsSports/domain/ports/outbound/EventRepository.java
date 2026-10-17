package com.drey.eventsSports.domain.ports.outbound;

import com.drey.eventsSports.domain.model.entities.SportEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EventRepository {
    Optional<SportEvent> findById(Long id);
    Page<SportEvent> findAll(Pageable pageable);
    SportEvent save(SportEvent event);
    void deleteById(Long id);
}
