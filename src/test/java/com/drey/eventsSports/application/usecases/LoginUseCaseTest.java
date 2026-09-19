package com.drey.eventsSports.application.usecases;

import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.model.exceptions.InvalidCredentialsException;
import com.drey.eventsSports.domain.model.exceptions.UnauthorizedRoleException;
import com.drey.eventsSports.domain.model.exceptions.UserInactiveException;
import com.drey.eventsSports.domain.ports.inbound.LoginCommand;
import com.drey.eventsSports.domain.ports.inbound.LoginResult;
import com.drey.eventsSports.domain.ports.outbound.PasswordEncoderPort;
import com.drey.eventsSports.domain.ports.outbound.TokenProviderPort;
import com.drey.eventsSports.domain.ports.outbound.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private TokenProviderPort tokenProvider;

    @InjectMocks
    private LoginUseCaseImpl loginUseCase;

    private User adminUser;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder()
                .id(1L)
                .code("ADMIN")
                .name("Administrator")
                .status(1)
                .build();

        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@sportsevents.com")
                .passwordHash("$2a$10$encryptedHash")
                .status(1)
                .roles(Set.of(adminRole))
                .build();
    }

    @Test
    @DisplayName("Should successfully authenticate administrator and return token")
    void shouldLoginSuccessfullyWhenCredentialsAreValid() {
        LoginCommand command = new LoginCommand("admin@sportsevents.com", "Prueba123+");

        when(userRepository.findByEmail("admin@sportsevents.com")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("Prueba123+", "$2a$10$encryptedHash")).thenReturn(true);
        when(tokenProvider.generateToken(adminUser)).thenReturn("jwt.token.value");
        when(tokenProvider.getExpirationSeconds()).thenReturn(86400L);

        LoginResult result = loginUseCase.login(command);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("jwt.token.value");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresIn()).isEqualTo(86400L);
        assertThat(result.user().getId()).isEqualTo(1L);
        assertThat(result.user().getEmail()).isEqualTo("admin@sportsevents.com");
        assertThat(result.user().getRoles()).extracting(Role::getCode).containsExactly("ADMIN");

        verify(tokenProvider).generateToken(adminUser);
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when email is not found")
    void shouldThrowWhenEmailNotFound() {
        LoginCommand command = new LoginCommand("nonexistent@sportsevents.com", "Prueba123+");

        when(userRepository.findByEmail("nonexistent@sportsevents.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.login(command))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password does not match")
    void shouldThrowWhenPasswordMismatch() {
        LoginCommand command = new LoginCommand("admin@sportsevents.com", "WrongPassword");

        when(userRepository.findByEmail("admin@sportsevents.com")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$encryptedHash")).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.login(command))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw UserInactiveException when user account is inactive")
    void shouldThrowWhenUserInactive() {
        User inactiveUser = User.builder()
                .id(2L)
                .username("inactiveAdmin")
                .email("admin@sportsevents.com")
                .passwordHash("$2a$10$encryptedHash")
                .status(0)
                .roles(Set.of(adminRole))
                .build();

        LoginCommand command = new LoginCommand("admin@sportsevents.com", "Prueba123+");

        when(userRepository.findByEmail("admin@sportsevents.com")).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> loginUseCase.login(command))
                .isInstanceOf(UserInactiveException.class)
                .hasMessage("Account is inactive");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedRoleException when user lacks ADMIN role")
    void shouldThrowWhenUserIsNotAdmin() {
        Role userRole = Role.builder()
                .id(2L)
                .code("OPERATOR")
                .name("Operator")
                .status(1)
                .build();

        User standardUser = User.builder()
                .id(3L)
                .username("regularuser")
                .email("user@sportsevents.com")
                .passwordHash("$2a$10$encryptedHash")
                .status(1)
                .roles(Set.of(userRole))
                .build();

        LoginCommand command = new LoginCommand("user@sportsevents.com", "Prueba123+");

        when(userRepository.findByEmail("user@sportsevents.com")).thenReturn(Optional.of(standardUser));
        when(passwordEncoder.matches("Prueba123+", "$2a$10$encryptedHash")).thenReturn(true);

        assertThatThrownBy(() -> loginUseCase.login(command))
                .isInstanceOf(UnauthorizedRoleException.class)
                .hasMessage("User does not have required administrator privileges");

        verify(tokenProvider, never()).generateToken(any());
    }
}
