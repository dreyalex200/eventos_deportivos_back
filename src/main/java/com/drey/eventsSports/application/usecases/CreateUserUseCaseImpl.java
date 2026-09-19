package com.drey.eventsSports.application.usecases;

import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.model.exceptions.DuplicateEmailException;
import com.drey.eventsSports.domain.model.exceptions.DuplicateUsernameException;
import com.drey.eventsSports.domain.ports.inbound.CreateUserCommand;
import com.drey.eventsSports.domain.ports.inbound.CreateUserUseCase;
import com.drey.eventsSports.domain.ports.outbound.PasswordEncoderPort;
import com.drey.eventsSports.domain.ports.outbound.RoleRepositoryPort;
import com.drey.eventsSports.domain.ports.outbound.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private static final String DEFAULT_ROLE = "OPERATOR";

    private final UserRepository userRepository;
    private final RoleRepositoryPort roleRepository;
    private final PasswordEncoderPort passwordEncoder;

    public CreateUserUseCaseImpl(
            UserRepository userRepository,
            RoleRepositoryPort roleRepository,
            PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(CreateUserCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("CreateUserCommand cannot be null");
        }

        String normalizedEmail = command.email().trim().toLowerCase();
        String normalizedUsername = command.username().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("User with email '" + normalizedEmail + "' already exists");
        }

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new DuplicateUsernameException("User with username '" + normalizedUsername + "' already exists");
        }

        String passwordHash = passwordEncoder.encode(command.password());

        Set<String> roleCodesToResolve = (command.roles() != null && !command.roles().isEmpty())
                ? command.roles()
                : Set.of(DEFAULT_ROLE);

        Set<Role> resolvedRoles = roleRepository.findAllByCodes(roleCodesToResolve);
        if (resolvedRoles.isEmpty()) {
            Role defaultRole = roleRepository.findByCode(DEFAULT_ROLE)
                    .orElseGet(() -> new Role(DEFAULT_ROLE, "Operator"));
            resolvedRoles = new HashSet<>();
            resolvedRoles.add(defaultRole);
        }

        User newUser = User.builder()
                .username(normalizedUsername)
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .firstName(command.firstName().trim())
                .lastName(command.lastName().trim())
                .phone(command.phone() != null ? command.phone().trim() : null)
                .status(1)
                .roles(resolvedRoles)
                .build();

        return userRepository.save(newUser);
    }
}
