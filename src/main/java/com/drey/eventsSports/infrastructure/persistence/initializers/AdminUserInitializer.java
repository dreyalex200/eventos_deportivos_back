package com.drey.eventsSports.infrastructure.persistence.initializers;

import com.drey.eventsSports.infrastructure.persistence.entities.PermissionJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.entities.RoleJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.entities.UserJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataPermissionRepository;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataRoleRepository;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class AdminUserInitializer implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@sportsevents.com";
    private static final String ADMIN_USERNAME = "admin";
    private static final String INITIAL_PASSWORD = "Prueba123+";
    private static final String ADMIN_ROLE = "ADMIN";

    private final SpringDataUserRepository userRepository;
    private final SpringDataRoleRepository roleRepository;
    private final SpringDataPermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(
            SpringDataUserRepository userRepository,
            SpringDataRoleRepository roleRepository,
            SpringDataPermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        PermissionJpaEntity createUsersPerm = permissionRepository.findByCode("USERS_CREATE")
                .orElseGet(() -> permissionRepository.save(PermissionJpaEntity.builder()
                        .code("USERS_CREATE")
                        .name("Create Users")
                        .description("Allows creating new users in the platform")
                        .status(1)
                        .build()));

        PermissionJpaEntity readUsersPerm = permissionRepository.findByCode("USERS_READ")
                .orElseGet(() -> permissionRepository.save(PermissionJpaEntity.builder()
                        .code("USERS_READ")
                        .name("Read Users")
                        .description("Allows reading user information and profiles")
                        .status(1)
                        .build()));

        RoleJpaEntity adminRole = roleRepository.findByCode(ADMIN_ROLE)
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder()
                        .code(ADMIN_ROLE)
                        .name("Administrator")
                        .description("Full platform administration")
                        .status(1)
                        .permissions(new HashSet<>(Set.of(createUsersPerm, readUsersPerm)))
                        .build()));

        if (adminRole.getPermissions() == null) {
            adminRole.setPermissions(new HashSet<>());
        }
        boolean updated = false;
        if (adminRole.getPermissions().stream().noneMatch(p -> "USERS_CREATE".equals(p.getCode()))) {
            adminRole.getPermissions().add(createUsersPerm);
            updated = true;
        }
        if (adminRole.getPermissions().stream().noneMatch(p -> "USERS_READ".equals(p.getCode()))) {
            adminRole.getPermissions().add(readUsersPerm);
            updated = true;
        }
        if (updated) {
            roleRepository.save(adminRole);
        }

        var existingUserOpt = userRepository.findByEmail(ADMIN_EMAIL);
        if (existingUserOpt.isPresent()) {
            UserJpaEntity existingUser = existingUserOpt.get();
            if (!passwordEncoder.matches(INITIAL_PASSWORD, existingUser.getPasswordHash())) {
                existingUser.setPasswordHash(passwordEncoder.encode(INITIAL_PASSWORD));
                userRepository.save(existingUser);
                log.info("Administrator user [{}] password hash updated to valid hash for '{}'.", ADMIN_EMAIL, INITIAL_PASSWORD);
            } else {
                log.info("Administrator user [{}] already exists with valid password. Skipping initialization.", ADMIN_EMAIL);
            }
            return;
        }

        UserJpaEntity adminUser = UserJpaEntity.builder()
                .username(ADMIN_USERNAME)
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(INITIAL_PASSWORD))
                .firstName("Admin")
                .lastName("SportsEvents")
                .phone("+573001234567")
                .status(1)
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(adminUser);
        log.info("Administrator user [{}] initialized successfully with role [{}] and permissions", ADMIN_EMAIL, ADMIN_ROLE);
    }
}
