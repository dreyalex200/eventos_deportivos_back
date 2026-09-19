package com.drey.eventsSports.infrastructure.controllers;

import com.drey.eventsSports.application.dtos.CreateUserRequest;
import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.model.exceptions.DuplicateEmailException;
import com.drey.eventsSports.domain.model.exceptions.DuplicateUsernameException;
import com.drey.eventsSports.domain.ports.inbound.CreateUserCommand;
import com.drey.eventsSports.domain.ports.inbound.CreateUserUseCase;
import com.drey.eventsSports.infrastructure.controllers.secured.UserController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateUserUseCase createUserUseCase;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User savedUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        Role operatorRole = Role.builder()
                .id(2L)
                .code("OPERATOR")
                .name("Operator")
                .status(1)
                .build();

        savedUser = User.builder()
                .id(5L)
                .username("newoperator")
                .email("operator@sportsevents.com")
                .passwordHash("$2a$10$verySecretHashNeverReturn")
                .firstName("Carlos")
                .lastName("Gomez")
                .phone("+573001112233")
                .status(1)
                .roles(Set.of(operatorRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/users/protected/users returns 201 Created on valid request")
    void shouldReturn201OnValidUserCreation() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "newoperator",
                "operator@sportsevents.com",
                "Secret123+",
                "Carlos",
                "Gomez",
                "+573001112233",
                Set.of("OPERATOR")
        );

        when(createUserUseCase.createUser(any(CreateUserCommand.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/v1/users/protected/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("User created successfully")))
                .andExpect(jsonPath("$.data.id", is(5)))
                .andExpect(jsonPath("$.data.username", is("newoperator")))
                .andExpect(jsonPath("$.data.email", is("operator@sportsevents.com")))
                .andExpect(jsonPath("$.data.firstName", is("Carlos")))
                .andExpect(jsonPath("$.data.lastName", is("Gomez")))
                .andExpect(jsonPath("$.data.phone", is("+573001112233")))
                .andExpect(jsonPath("$.data.roles[0]", is("OPERATOR")))
                // Critical security check: Never return passwords or hashes
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/users returns 201 Created on base path mapping")
    void shouldReturn201OnBasePathUserCreation() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "newoperator",
                "operator@sportsevents.com",
                "Secret123+",
                "Carlos",
                "Gomez",
                null,
                null
        );

        when(createUserUseCase.createUser(any(CreateUserCommand.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(5)));
    }

    @Test
    @DisplayName("POST /api/v1/users/protected/users returns 400 when email is invalid")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "newoperator",
                "not-an-email",
                "Secret123+",
                "Carlos",
                "Gomez",
                null,
                null
        );

        mockMvc.perform(post("/api/v1/users/protected/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("POST /api/v1/users/protected/users returns 400 when password is too short")
    void shouldReturn400WhenPasswordIsTooShort() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "newoperator",
                "operator@sportsevents.com",
                "123",
                "Carlos",
                "Gomez",
                null,
                null
        );

        mockMvc.perform(post("/api/v1/users/protected/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("POST /api/v1/users/protected/users returns 409 Conflict when email exists")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "newoperator",
                "existing@sportsevents.com",
                "Secret123+",
                "Carlos",
                "Gomez",
                null,
                null
        );

        when(createUserUseCase.createUser(any(CreateUserCommand.class)))
                .thenThrow(new DuplicateEmailException("User with email 'existing@sportsevents.com' already exists"));

        mockMvc.perform(post("/api/v1/users/protected/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.error.code", is("DUPLICATE_RESOURCE")))
                .andExpect(jsonPath("$.error.details", containsString("existing@sportsevents.com")));
    }

    @Test
    @DisplayName("POST /api/v1/users/protected/users returns 409 Conflict when username exists")
    void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "existinguser",
                "operator@sportsevents.com",
                "Secret123+",
                "Carlos",
                "Gomez",
                null,
                null
        );

        when(createUserUseCase.createUser(any(CreateUserCommand.class)))
                .thenThrow(new DuplicateUsernameException("User with username 'existinguser' already exists"));

        mockMvc.perform(post("/api/v1/users/protected/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.error.code", is("DUPLICATE_RESOURCE")))
                .andExpect(jsonPath("$.error.details", containsString("existinguser")));
    }
}
