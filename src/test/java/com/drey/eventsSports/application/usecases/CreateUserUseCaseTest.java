package com.drey.eventsSports.application.usecases;

import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.model.exceptions.DuplicateEmailException;
import com.drey.eventsSports.domain.model.exceptions.DuplicateUsernameException;
import com.drey.eventsSports.domain.ports.inbound.CreateUserCommand;
import com.drey.eventsSports.domain.ports.outbound.PasswordEncoderPort;
import com.drey.eventsSports.domain.ports.outbound.RoleRepositoryPort;
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
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepositoryPort roleRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private CreateUserUseCaseImpl createUserUseCase;

    private Role operatorRole;

    @BeforeEach
    void setUp() {
        operatorRole = Role.builder()
                .id(2L)
                .code("OPERATOR")
                .name("Operator")
                .status(1)
                .build();
    }

    @Test
    @DisplayName("Should successfully create user when data is valid")
    void shouldCreateUserSuccessfully() {
        CreateUserCommand command = new CreateUserCommand(
                "johndoe",
                "john@example.com",
                "Password123+",
                "John",
                "Doe",
                "+573001234567",
                Set.of("OPERATOR")
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(passwordEncoder.encode("Password123+")).thenReturn("$2a$10$hashedPassword");
        when(roleRepository.findAllByCodes(Set.of("OPERATOR"))).thenReturn(Set.of(operatorRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return User.builder()
                    .id(10L)
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .passwordHash(u.getPasswordHash())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .phone(u.getPhone())
                    .status(u.getStatus())
                    .roles(u.getRoles())
                    .build();
        });

        User result = createUserUseCase.createUser(command);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUsername()).isEqualTo("johndoe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPasswordHash()).isEqualTo("$2a$10$hashedPassword");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getPhone()).isEqualTo("+573001234567");
        assertThat(result.getRoles()).extracting(Role::getCode).containsExactly("OPERATOR");

        verify(passwordEncoder).encode("Password123+");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email already exists")
    void shouldThrowWhenEmailAlreadyExists() {
        CreateUserCommand command = new CreateUserCommand(
                "johndoe",
                "john@example.com",
                "Password123+",
                "John",
                "Doe",
                null,
                null
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> createUserUseCase.createUser(command))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("john@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateUsernameException when username already exists")
    void shouldThrowWhenUsernameAlreadyExists() {
        CreateUserCommand command = new CreateUserCommand(
                "johndoe",
                "john@example.com",
                "Password123+",
                "John",
                "Doe",
                null,
                null
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> createUserUseCase.createUser(command))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessageContaining("johndoe");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should use default role OPERATOR when roles are null or empty")
    void shouldUseDefaultRoleWhenNoneProvided() {
        CreateUserCommand command = new CreateUserCommand(
                "janedoe",
                "jane@example.com",
                "Password123+",
                "Jane",
                "Doe",
                null,
                null
        );

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("janedoe")).thenReturn(false);
        when(passwordEncoder.encode("Password123+")).thenReturn("$2a$10$hashedPassword");
        when(roleRepository.findAllByCodes(Set.of("OPERATOR"))).thenReturn(Set.of(operatorRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = createUserUseCase.createUser(command);

        assertThat(result.getRoles()).extracting(Role::getCode).containsExactly("OPERATOR");
        verify(userRepository).save(any(User.class));
    }
}
