package com.drey.eventsSports.infrastructure.controllers.anonymous;

import com.drey.eventsSports.application.dtos.ApiResponse;
import com.drey.eventsSports.application.dtos.LoginRequest;
import com.drey.eventsSports.application.dtos.LoginResponse;
import com.drey.eventsSports.application.dtos.UserDto;
import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.ports.inbound.LoginCommand;
import com.drey.eventsSports.domain.ports.inbound.LoginResult;
import com.drey.eventsSports.domain.ports.inbound.LoginUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.email(), request.password()));

        List<String> roleCodes = result.user().getRoles().stream()
                .map(Role::getCode)
                .sorted()
                .toList();

        UserDto userDto = new UserDto(
                result.user().getId(),
                result.user().getEmail(),
                roleCodes
        );

        LoginResponse loginResponse = new LoginResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresIn(),
                userDto
        );

        return ResponseEntity.ok(ApiResponse.success("Login successful", loginResponse));
    }
}
