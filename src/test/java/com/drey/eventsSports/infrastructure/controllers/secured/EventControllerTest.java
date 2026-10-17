package com.drey.eventsSports.infrastructure.controllers.secured;

import com.drey.eventsSports.domain.model.entities.SportEvent;
import com.drey.eventsSports.domain.model.exceptions.EventNotFoundException;
import com.drey.eventsSports.domain.ports.inbound.*;
import com.drey.eventsSports.infrastructure.controllers.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GetEventsUseCase getEventsUseCase;

    @Mock
    private GetEventByIdUseCase getEventByIdUseCase;

    @Mock
    private CreateEventUseCase createEventUseCase;

    @Mock
    private UpdateEventUseCase updateEventUseCase;

    @Mock
    private DeleteEventUseCase deleteEventUseCase;

    @InjectMocks
    private EventController eventController;

    private SportEvent sampleEvent;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleEvent = SportEvent.builder()
                .id(1L)
                .title("Torneo Apertura de Futbol 2026")
                .description("Campeonato aficionado categoria libre")
                .sportType("FOOTBALL")
                .location("Estadio Metropolitano")
                .startDate(LocalDateTime.of(2026, 10, 15, 9, 0, 0))
                .endDate(LocalDateTime.of(2026, 10, 20, 18, 0, 0))
                .status("PUBLISHED")
                .maxParticipants(16)
                .currentParticipants(8)
                .createdAt(LocalDateTime.of(2026, 10, 1, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 10, 1, 10, 0, 0))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/events returns 200 OK with paginated list of events")
    void shouldReturn200WithEventsPage() throws Exception {
        when(getEventsUseCase.getEvents(eq(0), eq(10), anyString()))
                .thenReturn(new PageImpl<>(List.of(sampleEvent), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/events?page=0&size=10&sort=startDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].id", is(1)))
                .andExpect(jsonPath("$.data.items[0].title", is("Torneo Apertura de Futbol 2026")))
                .andExpect(jsonPath("$.data.items[0].sportType", is("FOOTBALL")))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(10)))
                .andExpect(jsonPath("$.data.totalElements", is(1)))
                .andExpect(jsonPath("$.data.totalPages", is(1)));
    }

    @Test
    @DisplayName("GET /api/v1/events/{id} returns 200 OK with single event")
    void shouldReturn200WithEventDetail() throws Exception {
        when(getEventByIdUseCase.getEventById(1L)).thenReturn(sampleEvent);

        mockMvc.perform(get("/api/v1/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.title", is("Torneo Apertura de Futbol 2026")))
                .andExpect(jsonPath("$.data.location", is("Estadio Metropolitano")));
    }

    @Test
    @DisplayName("GET /api/v1/events/{id} returns 404 when event not found")
    void shouldReturn404WhenEventNotFound() throws Exception {
        when(getEventByIdUseCase.getEventById(99L))
                .thenThrow(new EventNotFoundException("Event not found with id: 99"));

        mockMvc.perform(get("/api/v1/events/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.error.code", is("EVENT_NOT_FOUND")));
    }

    @Test
    @DisplayName("DELETE /api/v1/events/{id} returns 200 OK when deleted successfully")
    void shouldReturn200WhenEventDeleted() throws Exception {
        doNothing().when(deleteEventUseCase).deleteEvent(1L);

        mockMvc.perform(delete("/api/v1/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")));
    }
}
