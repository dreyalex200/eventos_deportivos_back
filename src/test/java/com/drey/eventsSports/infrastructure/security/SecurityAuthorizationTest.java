package com.drey.eventsSports.infrastructure.security;

import com.drey.eventsSports.application.usecases.LoginUseCaseImpl;
import com.drey.eventsSports.domain.model.entities.Permission;
import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.ports.inbound.LoginCommand;
import com.drey.eventsSports.domain.ports.inbound.LoginResult;
import com.drey.eventsSports.domain.ports.outbound.UserRepository;
import com.drey.eventsSports.shared.constants.SecurityConstants;
import com.drey.eventsSports.shared.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityAuthorizationTest {

    private static final String SEED_ADMIN_HASH = "$2a$10$vW/3HAb0AfAGATzWlptP1e8oifwxR3gVd1FWO1/YhQkKxLCf0oi1q";
    private static final String SEED_ADMIN_PASSWORD = "Prueba123+";
    private static final String SEED_ADMIN_EMAIL = "admin@sportsevents.com";

    private JwtUtils jwtUtils;
    private JwtTokenProviderAdapter tokenProviderAdapter;
    private BcryptPasswordEncoderAdapter passwordEncoderAdapter;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(SecurityConstants.JWT_SECRET_DEFAULT, 3600);
        tokenProviderAdapter = new JwtTokenProviderAdapter(jwtUtils);
        passwordEncoderAdapter = new BcryptPasswordEncoderAdapter(new BCryptPasswordEncoder());
    }

    @Test
    @DisplayName("Should generate JWT containing both roles and effective permissions")
    void shouldGenerateJwtWithRolesAndPermissions() {
        Permission createPerm = new Permission(1L, "USERS_CREATE", "Create Users", "Desc", 1);
        Permission readPerm = new Permission(2L, "USERS_READ", "Read Users", "Desc", 1);

        Role adminRole = Role.builder()
                .id(1L)
                .code("ADMIN")
                .name("Administrator")
                .status(1)
                .permissions(Set.of(createPerm, readPerm))
                .build();

        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email(SEED_ADMIN_EMAIL)
                .passwordHash(SEED_ADMIN_HASH)
                .status(1)
                .roles(Set.of(adminRole))
                .build();

        String token = tokenProviderAdapter.generateToken(adminUser);

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.getUserIdFromToken(token)).isEqualTo("1");
        assertThat(jwtUtils.getEmailFromToken(token)).isEqualTo(SEED_ADMIN_EMAIL);
        assertThat(jwtUtils.getRolesFromToken(token)).containsExactly("ADMIN");
        assertThat(jwtUtils.getPermissionsFromToken(token)).containsExactlyInAnyOrder("USERS_CREATE", "USERS_READ");

        List<GrantedAuthority> authorities = jwtUtils.getAuthoritiesFromToken(token);
        List<String> authorityStrings = authorities.stream().map(GrantedAuthority::getAuthority).toList();

        assertThat(authorityStrings).contains("ROLE_ADMIN", "ADMIN", "USERS_CREATE", "USERS_READ");
    }

    @Test
    @DisplayName("Should successfully authenticate admin using seed password Prueba123+ and seed hash")
    void shouldSuccessfullyAuthenticateWithAdminSeedPasswordAndHash() {
        // 1. Validate BCrypt hash match directly
        boolean matches = passwordEncoderAdapter.matches(SEED_ADMIN_PASSWORD, SEED_ADMIN_HASH);
        assertThat(matches).as("BCrypt hash in 001_seed_admin.sql must match Prueba123+").isTrue();

        // 2. Validate full LoginUseCase authentication flow
        Permission createPerm = new Permission(1L, "USERS_CREATE", "Create Users", "Desc", 1);
        Permission readPerm = new Permission(2L, "USERS_READ", "Read Users", "Desc", 1);
        Role adminRole = Role.builder()
                .id(1L)
                .code("ADMIN")
                .name("Administrator")
                .status(1)
                .permissions(Set.of(createPerm, readPerm))
                .build();

        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email(SEED_ADMIN_EMAIL)
                .passwordHash(SEED_ADMIN_HASH)
                .status(1)
                .roles(Set.of(adminRole))
                .build();

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByEmail(SEED_ADMIN_EMAIL)).thenReturn(Optional.of(adminUser));

        LoginUseCaseImpl loginUseCase = new LoginUseCaseImpl(userRepository, passwordEncoderAdapter, tokenProviderAdapter);

        LoginResult result = loginUseCase.login(new LoginCommand(SEED_ADMIN_EMAIL, SEED_ADMIN_PASSWORD));

        assertThat(result).isNotNull();
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.user().getEmail()).isEqualTo(SEED_ADMIN_EMAIL);

        // Verify authorities inside the issued JWT
        List<GrantedAuthority> authorities = jwtUtils.getAuthoritiesFromToken(result.accessToken());
        List<String> authorityCodes = authorities.stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorityCodes).contains("ADMIN", "ROLE_ADMIN", "USERS_CREATE", "USERS_READ");
    }

    @Test
    @DisplayName("Should validate and reject expired or tampered tokens")
    void shouldRejectTamperedToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.invalidpayload.invalidsignature";
        assertThat(jwtUtils.validateToken(invalidToken)).isFalse();
    }
}
