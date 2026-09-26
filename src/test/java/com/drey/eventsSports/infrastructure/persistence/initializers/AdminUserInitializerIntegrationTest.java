package com.drey.eventsSports.infrastructure.persistence.initializers;

import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.ports.outbound.PasswordEncoderPort;
import com.drey.eventsSports.domain.ports.outbound.RoleRepositoryPort;
import com.drey.eventsSports.domain.ports.outbound.UserRepository;
import com.drey.eventsSports.infrastructure.persistence.adapters.RoleRepositoryAdapter;
import com.drey.eventsSports.infrastructure.persistence.adapters.UserRepositoryAdapter;
import com.drey.eventsSports.infrastructure.persistence.entities.PermissionJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.entities.RoleJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.entities.UserJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataPermissionRepository;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataRoleRepository;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataUserRepository;
import com.drey.eventsSports.infrastructure.security.BcryptPasswordEncoderAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserInitializerIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@sportsevents.com";
    private static final String ADMIN_PASSWORD = "Prueba123+";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_ROLE = "ADMIN";

    @Mock
    private SpringDataUserRepository springDataUserRepository;

    @Mock
    private SpringDataRoleRepository springDataRoleRepository;

    @Mock
    private SpringDataPermissionRepository springDataPermissionRepository;

    private PasswordEncoderPort passwordEncoderPort;
    private UserRepository userRepositoryAdapter;
    private RoleRepositoryPort roleRepositoryAdapter;
    private AdminUserInitializer initializer;

    private final Map<String, UserJpaEntity> userDatabase = new HashMap<>();
    private final Map<String, RoleJpaEntity> roleDatabase = new HashMap<>();
    private final Map<String, PermissionJpaEntity> permissionDatabase = new HashMap<>();

    @BeforeEach
    void setUp() {
        userDatabase.clear();
        roleDatabase.clear();
        permissionDatabase.clear();

        // Real BCrypt encoder adapter
        passwordEncoderPort = new BcryptPasswordEncoderAdapter(new BCryptPasswordEncoder());

        // Real persistence adapters
        userRepositoryAdapter = new UserRepositoryAdapter(springDataUserRepository, springDataRoleRepository);
        roleRepositoryAdapter = new RoleRepositoryAdapter(springDataRoleRepository);

        initializer = new AdminUserInitializer(
                ADMIN_EMAIL,
                ADMIN_PASSWORD,
                ADMIN_USERNAME,
                "Admin",
                "SportsEvents",
                "+573001234567",
                ADMIN_ROLE,
                userRepositoryAdapter,
                roleRepositoryAdapter,
                springDataRoleRepository,
                springDataPermissionRepository,
                passwordEncoderPort
        );

        // Configure SpringDataUserRepository mock backed by in-memory map
        when(springDataUserRepository.findByEmail(anyString())).thenAnswer(invocation -> {
            String email = invocation.getArgument(0);
            return Optional.ofNullable(userDatabase.get(email));
        });

        when(springDataUserRepository.save(any(UserJpaEntity.class))).thenAnswer(invocation -> {
            UserJpaEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId((long) (userDatabase.size() + 1));
            }
            userDatabase.put(entity.getEmail(), entity);
            return entity;
        });

        // Configure SpringDataRoleRepository mock backed by in-memory map
        when(springDataRoleRepository.findByCode(anyString())).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(roleDatabase.get(code));
        });

        when(springDataRoleRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return roleDatabase.values().stream().filter(r -> Objects.equals(r.getId(), id)).findFirst();
        });

        when(springDataRoleRepository.save(any(RoleJpaEntity.class))).thenAnswer(invocation -> {
            RoleJpaEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId((long) (roleDatabase.size() + 1));
            }
            roleDatabase.put(entity.getCode(), entity);
            return entity;
        });

        // Configure SpringDataPermissionRepository mock backed by in-memory map
        when(springDataPermissionRepository.findByCode(anyString())).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(permissionDatabase.get(code));
        });

        when(springDataPermissionRepository.save(any(PermissionJpaEntity.class))).thenAnswer(invocation -> {
            PermissionJpaEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId((long) (permissionDatabase.size() + 1));
            }
            permissionDatabase.put(entity.getCode(), entity);
            return entity;
        });
    }

    @Test
    @DisplayName("Complete initialization flow: creates admin with real BCrypt hash and assigned role, then skips on restart")
    void shouldExecuteCompleteInitializationFlowIdempotently() {
        // First startup: Admin does not exist
        initializer.initialize();

        assertThat(userDatabase).hasSize(1);
        UserJpaEntity savedEntity = userDatabase.get(ADMIN_EMAIL);
        assertThat(savedEntity).isNotNull();
        assertThat(savedEntity.getEmail()).isEqualTo(ADMIN_EMAIL);
        assertThat(savedEntity.getUsername()).isEqualTo(ADMIN_USERNAME);
        assertThat(savedEntity.getStatus()).isEqualTo(1);

        // Verify password was securely hashed via BCrypt and matches configured password
        assertThat(savedEntity.getPasswordHash()).isNotEqualTo(ADMIN_PASSWORD);
        assertThat(passwordEncoderPort.matches(ADMIN_PASSWORD, savedEntity.getPasswordHash())).isTrue();

        // Verify role and permissions
        assertThat(savedEntity.getRoles()).hasSize(1);
        RoleJpaEntity assignedRole = savedEntity.getRoles().iterator().next();
        assertThat(assignedRole.getCode()).isEqualTo(ADMIN_ROLE);
        assertThat(assignedRole.getPermissions()).extracting(PermissionJpaEntity::getCode)
                .containsExactlyInAnyOrder("USERS_CREATE", "USERS_READ");

        // Verify domain layer sees the user correctly
        Optional<User> domainUser = userRepositoryAdapter.findByEmail(ADMIN_EMAIL);
        assertThat(domainUser).isPresent();
        assertThat(domainUser.get().hasRole(ADMIN_ROLE)).isTrue();
        assertThat(domainUser.get().hasPermission("USERS_CREATE")).isTrue();
        assertThat(domainUser.get().hasPermission("USERS_READ")).isTrue();

        // Second startup: Admin already exists
        initializer.initialize();

        // Verify exactly one admin user exists and was not saved again
        assertThat(userDatabase).hasSize(1);
        verify(springDataUserRepository, times(1)).save(any(UserJpaEntity.class));

        // Third startup: Admin already exists
        initializer.initialize();
        assertThat(userDatabase).hasSize(1);
        verify(springDataUserRepository, times(1)).save(any(UserJpaEntity.class));
    }
}
