package com.drey.eventsSports.infrastructure.persistence.initializers;

import com.drey.eventsSports.infrastructure.persistence.entities.RoleJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.entities.UserJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataRoleRepository;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(
            SpringDataUserRepository userRepository,
            SpringDataRoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            log.info("Administrator user [{}] already exists. Skipping initialization.", ADMIN_EMAIL);
            return;
        }

        RoleJpaEntity adminRole = roleRepository.findByCode(ADMIN_ROLE)
                .orElseGet(() -> roleRepository.save(RoleJpaEntity.builder()
                        .code(ADMIN_ROLE)
                        .name("Administrator")
                        .description("Full platform administration")
                        .status(1)
                        .build()));

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
        log.info("Administrator user [{}] initialized successfully with role [{}]", ADMIN_EMAIL, ADMIN_ROLE);
    }
}
