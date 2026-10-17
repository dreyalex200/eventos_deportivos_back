package com.drey.eventsSports.application.usecases;

import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.model.exceptions.UserNotFoundException;
import com.drey.eventsSports.domain.ports.outbound.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetCurrentUserUseCaseImpl getCurrentUserUseCase;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@sportsevents.com")
                .firstName("Admin")
                .lastName("User")
                .build();
    }

    @Test
    @DisplayName("getCurrentUser by ID returns user when found")
    void shouldReturnUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        User user = getCurrentUserUseCase.getCurrentUser(1L);

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("admin", user.getUsername());
    }

    @Test
    @DisplayName("getCurrentUser by ID throws UserNotFoundException when not found")
    void shouldThrowExceptionWhenUserNotFoundById() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> getCurrentUserUseCase.getCurrentUser(99L));
    }

    @Test
    @DisplayName("getCurrentUser by ID throws IllegalArgumentException when id is null")
    void shouldThrowExceptionWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> getCurrentUserUseCase.getCurrentUser(null));
    }

    @Test
    @DisplayName("getCurrentUserByEmail returns user when found")
    void shouldReturnUserByEmail() {
        when(userRepository.findByEmail("admin@sportsevents.com")).thenReturn(Optional.of(sampleUser));

        User user = getCurrentUserUseCase.getCurrentUserByEmail("admin@sportsevents.com");

        assertNotNull(user);
        assertEquals("admin@sportsevents.com", user.getEmail());
    }

    @Test
    @DisplayName("getCurrentUserByEmail throws UserNotFoundException when not found")
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        when(userRepository.findByEmail("unknown@sportsevents.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> getCurrentUserUseCase.getCurrentUserByEmail("unknown@sportsevents.com"));
    }
}
