package com.drey.eventsSports.infrastructure.controllers.secured;

import com.drey.eventsSports.application.dtos.ApiResponse;
import com.drey.eventsSports.application.dtos.CreateUserRequest;
import com.drey.eventsSports.application.dtos.UserResponse;
import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.ports.inbound.CreateUserCommand;
import com.drey.eventsSports.domain.ports.inbound.CreateUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/users/protected", "/api/v1/users"})
public class UserController {

    private final CreateUserUseCase createUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    @PostMapping({"/users", ""})
    @PreAuthorize("hasAuthority('USERS_CREATE')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        CreateUserCommand command = new CreateUserCommand(
                request.username(),
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.roles()
        );

        User createdUser = createUserUseCase.createUser(command);

        List<String> roleCodes = createdUser.getRoles().stream()
                .map(Role::getCode)
                .sorted()
                .toList();

        UserResponse userResponse = new UserResponse(
                createdUser.getId(),
                createdUser.getUsername(),
                createdUser.getEmail(),
                createdUser.getFirstName(),
                createdUser.getLastName(),
                createdUser.getPhone(),
                createdUser.getStatus(),
                roleCodes,
                createdUser.getCreatedAt(),
                createdUser.getUpdatedAt()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", userResponse));
    }
}
