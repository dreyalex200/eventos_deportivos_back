package com.drey.eventsSports.infrastructure.persistence.initializers;

import com.drey.eventsSports.domain.model.entities.Permission;
import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.ports.outbound.PasswordEncoderPort;
import com.drey.eventsSports.domain.ports.outbound.RoleRepositoryPort;
import com.drey.eventsSports.domain.ports.outbound.UserRepository;
import com.drey.eventsSports.infrastructure.persistence.entities.PermissionJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.entities.RoleJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataPermissionRepository;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserInitializerTest {

    private static final String ADMIN_EMAIL = "admin@sportsevents.com";
    private static final String ADMIN_PASSWORD = "Prueba123+";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_FIRST_NAME = "Admin";
    private static final String ADMIN_LAST_NAME = "SportsEvents";
    private static final String ADMIN_PHONE = "+573001234567";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPasswordHashMock";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @Mock
    private SpringDataRoleRepository springDataRoleRepository;

    @Mock
    private SpringDataPermissionRepository springDataPermissionRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    private AdminUserInitializer initializer;

    private Role existingAdminRole;

    @BeforeEach
    void setUp() {
        initializer = new AdminUserInitializer(
                ADMIN_EMAIL,
                ADMIN_PASSWORD,
                ADMIN_USERNAME,
                ADMIN_FIRST_NAME,
                ADMIN_LAST_NAME,
                ADMIN_PHONE,
                ADMIN_ROLE,
                userRepository,
                roleRepositoryPort,
                springDataRoleRepository,
                springDataPermissionRepository,
                passwordEncoderPort
        );

        existingAdminRole = Role.builder()
                .id(1L)
                .code(ADMIN_ROLE)
                .name("Administrator")
                .status(1)
                .permissions(Set.of(
                        new Permission(1L, "USERS_CREATE", "Create Users", "Desc", 1),
                        new Permission(2L, "USERS_READ", "Read Users", "Desc", 1)
                ))
                .build();
    }

    @Nested
    @DisplayName("Scenario 1 — Admin does not exist")
    class AdminDoesNotExist {

        @Test
        @DisplayName("Should create and persist administrator when user does not exist")
        void shouldCreateAdminWhenNotExists() {
            when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.empty());
            when(roleRepositoryPort.findByCode(ADMIN_ROLE)).thenReturn(Optional.of(existingAdminRole));
            when(passwordEncoderPort.encode(ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            initializer.initialize();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getEmail()).isEqualTo(ADMIN_EMAIL);
            assertThat(savedUser.getUsername()).isEqualTo(ADMIN_USERNAME);
            assertThat(savedUser.getPasswordHash()).isEqualTo(ENCODED_PASSWORD);
            assertThat(savedUser.getFirstName()).isEqualTo(ADMIN_FIRST_NAME);
            assertThat(savedUser.getLastName()).isEqualTo(ADMIN_LAST_NAME);
            assertThat(savedUser.getPhone()).isEqualTo(ADMIN_PHONE);
            assertThat(savedUser.getStatus()).isEqualTo(1);
            assertThat(savedUser.getRoles()).containsExactly(existingAdminRole);
            assertThat(savedUser.hasRole(ADMIN_ROLE)).isTrue();

            verify(passwordEncoderPort).encode(ADMIN_PASSWORD);
            verify(springDataRoleRepository, never()).save(any());
            verify(springDataPermissionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Scenario 2 — Admin already exists")
    class AdminAlreadyExists {

        @Test
        @DisplayName("Should skip initialization and not overwrite user or password when admin already exists")
        void shouldDoNothingWhenAdminAlreadyExists() {
            User existingAdmin = User.builder()
                    .id(1L)
                    .username(ADMIN_USERNAME)
                    .email(ADMIN_EMAIL)
                    .passwordHash("$2a$10$previouslySavedHash")
                    .status(1)
                    .roles(Set.of(existingAdminRole))
                    .build();

            when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(existingAdmin));

            initializer.initialize();

            verify(userRepository, never()).save(any());
            verify(passwordEncoderPort, never()).encode(any());
            verify(roleRepositoryPort, never()).findByCode(any());
            verify(springDataRoleRepository, never()).save(any());
            verify(springDataPermissionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Scenario 3 — Multiple executions (Idempotency)")
    class MultipleExecutions {

        @Test
        @DisplayName("Multiple executions should result in exactly one persistence operation")
        void shouldBeIdempotentAcrossMultipleExecutions() {
            User createdAdmin = User.builder()
                    .id(1L)
                    .username(ADMIN_USERNAME)
                    .email(ADMIN_EMAIL)
                    .passwordHash(ENCODED_PASSWORD)
                    .status(1)
                    .roles(Set.of(existingAdminRole))
                    .build();

            // First call: user does not exist
            // Subsequent calls: user exists
            when(userRepository.findByEmail(ADMIN_EMAIL))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(createdAdmin))
                    .thenReturn(Optional.of(createdAdmin));

            when(roleRepositoryPort.findByCode(ADMIN_ROLE)).thenReturn(Optional.of(existingAdminRole));
            when(passwordEncoderPort.encode(ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            initializer.initialize();
            initializer.initialize();
            initializer.initialize();

            verify(userRepository, times(1)).save(any(User.class));
            verify(passwordEncoderPort, times(1)).encode(ADMIN_PASSWORD);
        }
    }

    @Nested
    @DisplayName("Scenario 4 — Password security")
    class PasswordSecurity {

        @Test
        @DisplayName("Stored password must NOT equal plaintext configured password and PasswordEncoderPort must be used")
        void shouldEncodePasswordSecurely() {
            when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.empty());
            when(roleRepositoryPort.findByCode(ADMIN_ROLE)).thenReturn(Optional.of(existingAdminRole));
            when(passwordEncoderPort.encode(ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            initializer.initialize();

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User user = captor.getValue();
            assertThat(user.getPasswordHash()).isNotEqualTo(ADMIN_PASSWORD);
            assertThat(user.getPasswordHash()).isEqualTo(ENCODED_PASSWORD);
            verify(passwordEncoderPort).encode(ADMIN_PASSWORD);
        }
    }

    @Nested
    @DisplayName("Scenario 5 — Existing role vs Missing role")
    class RoleHandling {

        @Test
        @DisplayName("When ADMIN role already exists, it is reused and no new role is created")
        void shouldReuseExistingRole() {
            when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.empty());
            when(roleRepositoryPort.findByCode(ADMIN_ROLE)).thenReturn(Optional.of(existingAdminRole));
            when(passwordEncoderPort.encode(ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            initializer.initialize();

            verify(springDataRoleRepository, never()).save(any());
            verify(springDataPermissionRepository, never()).save(any());
        }

        @Test
        @DisplayName("When ADMIN role is missing, it should create permissions and role idempotently")
        void shouldCreateRoleAndPermissionsWhenMissing() {
            when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.empty());
            // Initially missing from roleRepositoryPort, then present after creation
            when(roleRepositoryPort.findByCode(ADMIN_ROLE))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(existingAdminRole));

            PermissionJpaEntity permCreate = PermissionJpaEntity.builder()
                    .id(1L)
                    .code("USERS_CREATE")
                    .name("Create Users")
                    .status(1)
                    .build();

            PermissionJpaEntity permRead = PermissionJpaEntity.builder()
                    .id(2L)
                    .code("USERS_READ")
                    .name("Read Users")
                    .status(1)
                    .build();

            when(springDataPermissionRepository.findByCode("USERS_CREATE")).thenReturn(Optional.of(permCreate));
            when(springDataPermissionRepository.findByCode("USERS_READ")).thenReturn(Optional.of(permRead));

            RoleJpaEntity roleJpaEntity = RoleJpaEntity.builder()
                    .id(1L)
                    .code(ADMIN_ROLE)
                    .name("Administrator")
                    .status(1)
                    .permissions(Set.of(permCreate, permRead))
                    .build();

            when(springDataRoleRepository.findByCode(ADMIN_ROLE)).thenReturn(Optional.of(roleJpaEntity));
            when(passwordEncoderPort.encode(ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            initializer.initialize();

            verify(userRepository).save(any(User.class));
            verify(roleRepositoryPort, times(2)).findByCode(ADMIN_ROLE);
        }
    }

    @Nested
    @DisplayName("Scenario 6 — Missing required configuration")
    class MissingConfiguration {

        @Test
        @DisplayName("Should throw IllegalStateException when admin email is blank")
        void shouldThrowWhenEmailIsBlank() {
            AdminUserInitializer invalidInitializer = new AdminUserInitializer(
                    "   ",
                    ADMIN_PASSWORD,
                    ADMIN_USERNAME,
                    ADMIN_FIRST_NAME,
                    ADMIN_LAST_NAME,
                    ADMIN_PHONE,
                    ADMIN_ROLE,
                    userRepository,
                    roleRepositoryPort,
                    springDataRoleRepository,
                    springDataPermissionRepository,
                    passwordEncoderPort
            );

            assertThatThrownBy(invalidInitializer::initialize)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Admin email must be configured");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when admin password is blank")
        void shouldThrowWhenPasswordIsBlank() {
            AdminUserInitializer invalidInitializer = new AdminUserInitializer(
                    ADMIN_EMAIL,
                    "",
                    ADMIN_USERNAME,
                    ADMIN_FIRST_NAME,
                    ADMIN_LAST_NAME,
                    ADMIN_PHONE,
                    ADMIN_ROLE,
                    userRepository,
                    roleRepositoryPort,
                    springDataRoleRepository,
                    springDataPermissionRepository,
                    passwordEncoderPort
            );

            assertThatThrownBy(invalidInitializer::initialize)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Admin password must be configured");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when admin username is blank")
        void shouldThrowWhenUsernameIsBlank() {
            AdminUserInitializer invalidInitializer = new AdminUserInitializer(
                    ADMIN_EMAIL,
                    ADMIN_PASSWORD,
                    null,
                    ADMIN_FIRST_NAME,
                    ADMIN_LAST_NAME,
                    ADMIN_PHONE,
                    ADMIN_ROLE,
                    userRepository,
                    roleRepositoryPort,
                    springDataRoleRepository,
                    springDataPermissionRepository,
                    passwordEncoderPort
            );

            assertThatThrownBy(invalidInitializer::initialize)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Admin username must be configured");
        }
    }

    @Nested
    @DisplayName("Scenario 7 — Concurrency race and duplicate prevention")
    class ConcurrencyAndDuplicatePrevention {

        @Test
        @DisplayName("Should gracefully skip when DataIntegrityViolationException occurs and admin was created concurrently")
        void shouldHandleConcurrentCreationGracefully() {
            User concurrentlyCreatedAdmin = User.builder()
                    .id(1L)
                    .username(ADMIN_USERNAME)
                    .email(ADMIN_EMAIL)
                    .passwordHash(ENCODED_PASSWORD)
                    .status(1)
                    .roles(Set.of(existingAdminRole))
                    .build();

            // Initial check: not found
            // After save failure: found (committed by other instance)
            when(userRepository.findByEmail(ADMIN_EMAIL))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(concurrentlyCreatedAdmin));

            when(roleRepositoryPort.findByCode(ADMIN_ROLE)).thenReturn(Optional.of(existingAdminRole));
            when(passwordEncoderPort.encode(ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            doThrow(new DataIntegrityViolationException("Duplicate entry 'admin@sportsevents.com' for key 'uq_users_email'"))
                    .when(userRepository).save(any(User.class));

            // Should not throw
            initializer.initialize();

            verify(userRepository, times(2)).findByEmail(ADMIN_EMAIL);
        }

        @Test
        @DisplayName("Should rethrow DataIntegrityViolationException if user still does not exist (unrelated failure)")
        void shouldRethrowIntegrityViolationIfUserStillMissing() {
            when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.empty());
            when(roleRepositoryPort.findByCode(ADMIN_ROLE)).thenReturn(Optional.of(existingAdminRole));
            when(passwordEncoderPort.encode(ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            doThrow(new DataIntegrityViolationException("Database constraint error"))
                    .when(userRepository).save(any(User.class));

            assertThatThrownBy(initializer::initialize)
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("Database constraint error");
        }
    }

    @Nested
    @DisplayName("Email Normalization and Case Matching")
    class EmailNormalization {

        @Test
        @DisplayName("Should normalize mixed-case email to lowercase and trim spaces")
        void shouldNormalizeEmailProperly() {
            AdminUserInitializer mixedCaseInitializer = new AdminUserInitializer(
                    "  Admin@SportsEvents.COM  ",
                    ADMIN_PASSWORD,
                    " admin ",
                    ADMIN_FIRST_NAME,
                    ADMIN_LAST_NAME,
                    ADMIN_PHONE,
                    ADMIN_ROLE,
                    userRepository,
                    roleRepositoryPort,
                    springDataRoleRepository,
                    springDataPermissionRepository,
                    passwordEncoderPort
            );

            when(userRepository.findByEmail("admin@sportsevents.com")).thenReturn(Optional.empty());
            when(roleRepositoryPort.findByCode(ADMIN_ROLE)).thenReturn(Optional.of(existingAdminRole));
            when(passwordEncoderPort.encode(ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            mixedCaseInitializer.initialize();

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User user = captor.getValue();
            assertThat(user.getEmail()).isEqualTo("admin@sportsevents.com");
            assertThat(user.getUsername()).isEqualTo("admin");
        }

        @Test
        @DisplayName("Should detect existing admin even when configured with mixed case")
        void shouldDetectExistingAdminWithMixedCaseConfig() {
            AdminUserInitializer mixedCaseInitializer = new AdminUserInitializer(
                    "Admin@SportsEvents.COM",
                    ADMIN_PASSWORD,
                    ADMIN_USERNAME,
                    ADMIN_FIRST_NAME,
                    ADMIN_LAST_NAME,
                    ADMIN_PHONE,
                    ADMIN_ROLE,
                    userRepository,
                    roleRepositoryPort,
                    springDataRoleRepository,
                    springDataPermissionRepository,
                    passwordEncoderPort
            );

            User existingUser = User.builder()
                    .id(1L)
                    .email("admin@sportsevents.com")
                    .username("admin")
                    .passwordHash("$2a$10$hash")
                    .status(1)
                    .roles(Set.of(existingAdminRole))
                    .build();

            when(userRepository.findByEmail("admin@sportsevents.com")).thenReturn(Optional.of(existingUser));

            mixedCaseInitializer.initialize();

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("ApplicationRunner execution")
    class ApplicationRunnerExecution {

        @Test
        @DisplayName("run(ApplicationArguments) should invoke initialize()")
        void shouldExecuteViaApplicationRunner() {
            ApplicationArguments args = mock(ApplicationArguments.class);

            when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.empty());
            when(roleRepositoryPort.findByCode(ADMIN_ROLE)).thenReturn(Optional.of(existingAdminRole));
            when(passwordEncoderPort.encode(ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            initializer.run(args);

            verify(userRepository).save(any(User.class));
        }
    }
}
