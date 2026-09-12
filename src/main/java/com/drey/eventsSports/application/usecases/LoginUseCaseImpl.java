package com.drey.eventsSports.application.usecases;

import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.model.exceptions.InvalidCredentialsException;
import com.drey.eventsSports.domain.model.exceptions.UnauthorizedRoleException;
import com.drey.eventsSports.domain.model.exceptions.UserInactiveException;
import com.drey.eventsSports.domain.ports.inbound.LoginCommand;
import com.drey.eventsSports.domain.ports.inbound.LoginResult;
import com.drey.eventsSports.domain.ports.inbound.LoginUseCase;
import com.drey.eventsSports.domain.ports.outbound.PasswordEncoderPort;
import com.drey.eventsSports.domain.ports.outbound.TokenProviderPort;
import com.drey.eventsSports.domain.ports.outbound.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCaseImpl implements LoginUseCase {

    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;

    public LoginUseCaseImpl(
            UserRepository userRepository,
            PasswordEncoderPort passwordEncoder,
            TokenProviderPort tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        if (command == null || command.email() == null || command.password() == null) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(command.email().trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UserInactiveException("Account is inactive");
        }

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.hasRole(ADMIN_ROLE)) {
            throw new UnauthorizedRoleException("User does not have required administrator privileges");
        }

        String accessToken = tokenProvider.generateToken(user);
        long expiresIn = tokenProvider.getExpirationSeconds();

        return new LoginResult(accessToken, "Bearer", expiresIn, user);
    }
}
