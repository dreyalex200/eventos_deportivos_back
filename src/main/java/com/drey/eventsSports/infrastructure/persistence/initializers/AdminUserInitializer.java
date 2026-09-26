package com.drey.eventsSports.infrastructure.persistence.initializers;

import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.ports.outbound.PasswordEncoderPort;
import com.drey.eventsSports.domain.ports.outbound.RoleRepositoryPort;
import com.drey.eventsSports.domain.ports.outbound.UserRepository;
import com.drey.eventsSports.infrastructure.persistence.entities.PermissionJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.entities.RoleJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataPermissionRepository;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class AdminUserInitializer implements ApplicationRunner {

    public static final String DEFAULT_ADMIN_EMAIL = "admin@sportsevents.com";
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "Prueba123+";
    public static final String DEFAULT_ADMIN_FIRST_NAME = "Admin";
    public static final String DEFAULT_ADMIN_LAST_NAME = "SportsEvents";
    public static final String DEFAULT_ADMIN_PHONE = "+573001234567";
    public static final String DEFAULT_ADMIN_ROLE = "ADMIN";

    private final String adminEmail;
    private final String adminPassword;
    private final String adminUsername;
    private final String adminFirstName;
    private final String adminLastName;
    private final String adminPhone;
    private final String adminRoleCode;

    private final UserRepository userRepository;
    private final RoleRepositoryPort roleRepositoryPort;
    private final SpringDataRoleRepository springDataRoleRepository;
    private final SpringDataPermissionRepository springDataPermissionRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public AdminUserInitializer(
            @Value("${app.security.admin.email:" + DEFAULT_ADMIN_EMAIL + "}") String adminEmail,
            @Value("${app.security.admin.password:" + DEFAULT_ADMIN_PASSWORD + "}") String adminPassword,
            @Value("${app.security.admin.username:" + DEFAULT_ADMIN_USERNAME + "}") String adminUsername,
            @Value("${app.security.admin.first-name:" + DEFAULT_ADMIN_FIRST_NAME + "}") String adminFirstName,
            @Value("${app.security.admin.last-name:" + DEFAULT_ADMIN_LAST_NAME + "}") String adminLastName,
            @Value("${app.security.admin.phone:" + DEFAULT_ADMIN_PHONE + "}") String adminPhone,
            @Value("${app.security.admin.role:" + DEFAULT_ADMIN_ROLE + "}") String adminRoleCode,
            UserRepository userRepository,
            RoleRepositoryPort roleRepositoryPort,
            SpringDataRoleRepository springDataRoleRepository,
            SpringDataPermissionRepository springDataPermissionRepository,
            PasswordEncoderPort passwordEncoderPort) {
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminUsername = adminUsername;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
        this.adminPhone = adminPhone;
        this.adminRoleCode = adminRoleCode;
        this.userRepository = userRepository;
        this.roleRepositoryPort = roleRepositoryPort;
        this.springDataRoleRepository = springDataRoleRepository;
        this.springDataPermissionRepository = springDataPermissionRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    public AdminUserInitializer(
            UserRepository userRepository,
            RoleRepositoryPort roleRepositoryPort,
            SpringDataRoleRepository springDataRoleRepository,
            SpringDataPermissionRepository springDataPermissionRepository,
            PasswordEncoderPort passwordEncoderPort) {
        this(
                DEFAULT_ADMIN_EMAIL,
                DEFAULT_ADMIN_PASSWORD,
                DEFAULT_ADMIN_USERNAME,
                DEFAULT_ADMIN_FIRST_NAME,
                DEFAULT_ADMIN_LAST_NAME,
                DEFAULT_ADMIN_PHONE,
                DEFAULT_ADMIN_ROLE,
                userRepository,
                roleRepositoryPort,
                springDataRoleRepository,
                springDataPermissionRepository,
                passwordEncoderPort
        );
    }

    @Override
    public void run(ApplicationArguments args) {
        initialize();
    }

    public void initialize() {
        validateConfiguration();

        String normalizedEmail = adminEmail.trim().toLowerCase();

        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);
        if (existingUser.isPresent()) {
            log.info("Administrator user already exists. Initialization skipped.");
            return;
        }

        log.info("Administrator user does not exist. Creating administrator user.");

        try {
            createAndPersistAdminUser(normalizedEmail);
            log.info("Administrator user created successfully.");
        } catch (DataIntegrityViolationException ex) {
            if (userRepository.findByEmail(normalizedEmail).isPresent()) {
                log.info("Administrator user already exists. Initialization skipped.");
            } else {
                log.error("Failed to initialize administrator user due to database constraint violation: {}", ex.getMessage());
                throw ex;
            }
        }
    }

    @Transactional
    protected void createAndPersistAdminUser(String normalizedEmail) {
        Role adminRole = getOrCreateAdminRole();
        String passwordHash = passwordEncoderPort.encode(adminPassword);

        User adminUser = User.builder()
                .username(adminUsername.trim())
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .firstName(adminFirstName != null ? adminFirstName.trim() : "")
                .lastName(adminLastName != null ? adminLastName.trim() : "")
                .phone(adminPhone != null && !adminPhone.isBlank() ? adminPhone.trim() : null)
                .status(1)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(adminUser);
    }

    @Transactional
    protected Role getOrCreateAdminRole() {
        Optional<Role> existingRole = roleRepositoryPort.findByCode(adminRoleCode);
        if (existingRole.isPresent()) {
            return existingRole.get();
        }

        if (springDataPermissionRepository != null && springDataRoleRepository != null) {
            PermissionJpaEntity createUsersPerm = springDataPermissionRepository.findByCode("USERS_CREATE")
                    .orElseGet(() -> springDataPermissionRepository.save(PermissionJpaEntity.builder()
                            .code("USERS_CREATE")
                            .name("Create Users")
                            .description("Allows creating new users in the platform")
                            .status(1)
                            .build()));

            PermissionJpaEntity readUsersPerm = springDataPermissionRepository.findByCode("USERS_READ")
                    .orElseGet(() -> springDataPermissionRepository.save(PermissionJpaEntity.builder()
                            .code("USERS_READ")
                            .name("Read Users")
                            .description("Allows reading user information and profiles")
                            .status(1)
                            .build()));

            RoleJpaEntity adminRoleEntity = springDataRoleRepository.findByCode(adminRoleCode)
                    .orElseGet(() -> springDataRoleRepository.save(RoleJpaEntity.builder()
                            .code(adminRoleCode)
                            .name("Administrator")
                            .description("Full platform administration")
                            .status(1)
                            .permissions(new HashSet<>(Set.of(createUsersPerm, readUsersPerm)))
                            .build()));

            if (adminRoleEntity.getPermissions() == null) {
                adminRoleEntity.setPermissions(new HashSet<>());
            }
            boolean updated = false;
            if (adminRoleEntity.getPermissions().stream().noneMatch(p -> "USERS_CREATE".equals(p.getCode()))) {
                adminRoleEntity.getPermissions().add(createUsersPerm);
                updated = true;
            }
            if (adminRoleEntity.getPermissions().stream().noneMatch(p -> "USERS_READ".equals(p.getCode()))) {
                adminRoleEntity.getPermissions().add(readUsersPerm);
                updated = true;
            }
            if (updated) {
                springDataRoleRepository.save(adminRoleEntity);
            }
        }

        return roleRepositoryPort.findByCode(adminRoleCode)
                .orElseThrow(() -> new IllegalStateException("Failed to resolve administrator role: " + adminRoleCode));
    }

    private void validateConfiguration() {
        if (adminEmail == null || adminEmail.isBlank()) {
            throw new IllegalStateException("Administrator user initialization failed: Admin email must be configured.");
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException("Administrator user initialization failed: Admin password must be configured.");
        }
        if (adminUsername == null || adminUsername.isBlank()) {
            throw new IllegalStateException("Administrator user initialization failed: Admin username must be configured.");
        }
    }
}
