package com.drey.eventsSports.infrastructure.controllers;

import com.drey.eventsSports.application.dtos.LoginRequest;
import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.model.exceptions.InvalidCredentialsException;
import com.drey.eventsSports.domain.model.exceptions.UnauthorizedRoleException;
import com.drey.eventsSports.domain.model.exceptions.UserInactiveException;
import com.drey.eventsSports.domain.ports.inbound.LoginCommand;
import com.drey.eventsSports.domain.ports.inbound.LoginResult;
import com.drey.eventsSports.domain.ports.inbound.LoginUseCase;
import com.drey.eventsSports.infrastructure.controllers.anonymous.AuthController;
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

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LoginUseCase loginUseCase;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User adminUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        Role adminRole = Role.builder()
                .id(1L)
                .code("ADMIN")
                .name("Administrator")
                .status(1)
                .build();

        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@sportsevents.com")
                .passwordHash("$2a$10$hash")
                .status(1)
                .roles(Set.of(adminRole))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 200 OK with JWT token and envelope")
    void shouldReturn200OnSuccessfulLogin() throws Exception {
        LoginRequest request = new LoginRequest("admin@sportsevents.com", "Prueba123+");
        LoginResult result = new LoginResult("jwt.mock.token", "Bearer", 86400L, adminUser);

        when(loginUseCase.login(any(LoginCommand.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Login successful")))
                .andExpect(jsonPath("$.data.accessToken", is("jwt.mock.token")))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.expiresIn", is(86400)))
                .andExpect(jsonPath("$.data.user.email", is("admin@sportsevents.com")))
                .andExpect(jsonPath("$.data.user.roles[0]", is("ADMIN")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/anonymous/login returns 200 OK with JWT token and envelope")
    void shouldReturn200OnSuccessfulAnonymousLogin() throws Exception {
        LoginRequest request = new LoginRequest("admin@sportsevents.com", "Prueba123+");
        LoginResult result = new LoginResult("jwt.mock.token", "Bearer", 86400L, adminUser);

        when(loginUseCase.login(any(LoginCommand.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/auth/anonymous/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Login successful")))
                .andExpect(jsonPath("$.data.accessToken", is("jwt.mock.token")))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.expiresIn", is(86400)))
                .andExpect(jsonPath("$.data.user.email", is("admin@sportsevents.com")))
                .andExpect(jsonPath("$.data.user.roles[0]", is("ADMIN")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 400 Bad Request when email is blank")
    void shouldReturn400WhenEmailIsBlank() throws Exception {
        LoginRequest request = new LoginRequest("", "Prueba123+");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 400 Bad Request when email format is invalid")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        LoginRequest request = new LoginRequest("invalid-email-address", "Prueba123+");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.error.details", containsString("valid email")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 401 Unauthorized when credentials invalid")
    void shouldReturn401WhenCredentialsInvalid() throws Exception {
        LoginRequest request = new LoginRequest("admin@sportsevents.com", "WrongPassword");

        when(loginUseCase.login(any(LoginCommand.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.error.code", is("INVALID_CREDENTIALS")))
                .andExpect(jsonPath("$.error.details", is("Invalid email or password")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 401 Unauthorized when account inactive")
    void shouldReturn401WhenAccountInactive() throws Exception {
        LoginRequest request = new LoginRequest("admin@sportsevents.com", "Prueba123+");

        when(loginUseCase.login(any(LoginCommand.class)))
                .thenThrow(new UserInactiveException("Account is inactive"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.error.code", is("USER_INACTIVE")))
                .andExpect(jsonPath("$.error.details", is("Account is inactive")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 403 Forbidden when user lacks ADMIN role")
    void shouldReturn403WhenUserLacksAdminRole() throws Exception {
        LoginRequest request = new LoginRequest("user@sportsevents.com", "Prueba123+");

        when(loginUseCase.login(any(LoginCommand.class)))
                .thenThrow(new UnauthorizedRoleException("User does not have required administrator privileges"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.error.code", is("FORBIDDEN_ROLE")))
                .andExpect(jsonPath("$.error.details", is("User does not have required administrator privileges")));
    }
}
