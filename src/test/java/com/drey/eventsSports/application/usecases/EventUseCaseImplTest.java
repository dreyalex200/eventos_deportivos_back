package com.drey.eventsSports.application.usecases;

import com.drey.eventsSports.domain.model.entities.SportEvent;
import com.drey.eventsSports.domain.model.exceptions.EventNotFoundException;
import com.drey.eventsSports.domain.ports.inbound.CreateEventCommand;
import com.drey.eventsSports.domain.ports.inbound.UpdateEventCommand;
import com.drey.eventsSports.domain.ports.outbound.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventUseCaseImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventUseCaseImpl eventUseCase;

    private SportEvent sampleEvent;

    @BeforeEach
    void setUp() {
        sampleEvent = SportEvent.builder()
                .id(1L)
                .title("Torneo Apertura de Futbol 2026")
                .description("Campeonato aficionado categoria libre")
                .sportType("FOOTBALL")
                .location("Estadio Metropolitano")
                .startDate(LocalDateTime.of(2026, 10, 15, 9, 0, 0))
                .endDate(LocalDateTime.of(2026, 10, 20, 18, 0, 0))
                .status("DRAFT")
                .maxParticipants(16)
                .currentParticipants(0)
                .build();
    }

    @Test
    @DisplayName("getEvents returns page of events")
    void shouldReturnEventsPage() {
        when(eventRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleEvent)));

        Page<SportEvent> result = eventUseCase.getEvents(0, 10, "startDate,desc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Torneo Apertura de Futbol 2026", result.getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("getEventById returns event when found")
    void shouldReturnEventById() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        SportEvent result = eventUseCase.getEventById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("getEventById throws EventNotFoundException when event not found")
    void shouldThrowWhenEventNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> eventUseCase.getEventById(99L));
    }

    @Test
    @DisplayName("createEvent successfully creates new event")
    void shouldCreateEvent() {
        CreateEventCommand command = new CreateEventCommand(
                "Torneo Apertura",
                "Descripcion",
                "FOOTBALL",
                "Cancha 1",
                LocalDateTime.of(2026, 11, 1, 10, 0),
                LocalDateTime.of(2026, 11, 5, 18, 0),
                20
        );

        when(eventRepository.save(any(SportEvent.class))).thenReturn(sampleEvent);

        SportEvent created = eventUseCase.createEvent(command);

        assertNotNull(created);
        verify(eventRepository, times(1)).save(any(SportEvent.class));
    }

    @Test
    @DisplayName("createEvent throws IllegalArgumentException when startDate is after endDate")
    void shouldThrowWhenDatesInvalid() {
        CreateEventCommand command = new CreateEventCommand(
                "Torneo Apertura",
                "Descripcion",
                "FOOTBALL",
                "Cancha 1",
                LocalDateTime.of(2026, 11, 10, 10, 0),
                LocalDateTime.of(2026, 11, 5, 18, 0),
                20
        );

        assertThrows(IllegalArgumentException.class, () -> eventUseCase.createEvent(command));
    }

    @Test
    @DisplayName("deleteEvent calls repository deleteById")
    void shouldDeleteEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));
        doNothing().when(eventRepository).deleteById(1L);

        assertDoesNotThrow(() -> eventUseCase.deleteEvent(1L));
        verify(eventRepository, times(1)).deleteById(1L);
    }
}
