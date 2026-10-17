package com.drey.eventsSports.infrastructure.controllers.secured;

import com.drey.eventsSports.application.dtos.ApiResponse;
import com.drey.eventsSports.application.dtos.CreateUserRequest;
import com.drey.eventsSports.application.dtos.UserResponse;
import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.model.exceptions.UserNotFoundException;
import com.drey.eventsSports.domain.ports.inbound.CreateUserCommand;
import com.drey.eventsSports.domain.ports.inbound.CreateUserUseCase;
import com.drey.eventsSports.domain.ports.inbound.GetCurrentUserUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/users/protected", "/api/v1/users"})
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase, GetCurrentUserUseCase getCurrentUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            HttpServletRequest request,
            Authentication authentication) {
        Long userId = null;
        Object userIdAttr = request.getAttribute("user_id");
        if (userIdAttr instanceof String s && !s.isBlank()) {
            try {
                userId = Long.parseLong(s);
            } catch (NumberFormatException ignored) {
            }
        }

        User user;
        if (userId != null) {
            user = getCurrentUserUseCase.getCurrentUser(userId);
        } else if (authentication != null && authentication.getName() != null) {
            user = getCurrentUserUseCase.getCurrentUserByEmail(authentication.getName());
        } else {
            throw new UserNotFoundException("Authenticated user not found");
        }

        List<String> roleCodes = user.getRoles().stream()
                .map(Role::getCode)
                .sorted()
                .toList();

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getStatus(),
                roleCodes,
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", userResponse));
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
                createdUser.getLastLoginAt(),
                createdUser.getCreatedAt(),
                createdUser.getUpdatedAt()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", userResponse));
    }
}
