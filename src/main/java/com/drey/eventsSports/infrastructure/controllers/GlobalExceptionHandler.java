
package com.drey.eventsSports.infrastructure.controllers;

import com.drey.eventsSports.domain.model.exceptions.InvalidCredentialsException;
import com.drey.eventsSports.domain.model.exceptions.UnauthorizedRoleException;
import com.drey.eventsSports.domain.model.exceptions.UserInactiveException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .error(ErrorResponse.ErrorDetails.builder()
                        .code("INVALID_CREDENTIALS")
                        .details(ex.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .requestId(UUID.randomUUID())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserInactiveException.class)
    public ResponseEntity<ErrorResponse> handleUserInactive(UserInactiveException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .error(ErrorResponse.ErrorDetails.builder()
                        .code("USER_INACTIVE")
                        .details(ex.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .requestId(UUID.randomUUID())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UnauthorizedRoleException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedRole(UnauthorizedRoleException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .error(ErrorResponse.ErrorDetails.builder()
                        .code("FORBIDDEN_ROLE")
                        .details(ex.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .requestId(UUID.randomUUID())
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .error(ErrorResponse.ErrorDetails.builder()
                        .code("VALIDATION_ERROR")
                        .details(details)
                        .build())
                .timestamp(LocalDateTime.now())
                .requestId(UUID.randomUUID())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .error(ErrorResponse.ErrorDetails.builder()
                        .code("INVALID_CREDENTIALS")
                        .details(ex.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .requestId(UUID.randomUUID())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .error(ErrorResponse.ErrorDetails.builder()
                        .code("ACCESS_DENIED")
                        .details(ex.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .requestId(UUID.randomUUID())
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .error(ErrorResponse.ErrorDetails.builder()
                        .code("ROUTE_NOT_FOUND")
                        .details("no route found for " + ex.getHttpMethod() + " " + ex.getRequestURL())
                        .build())
                .timestamp(LocalDateTime.now())
                .requestId(UUID.randomUUID())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(com.drey.eventsSports.domain.model.exceptions.DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(com.drey.eventsSports.domain.model.exceptions.DuplicateEmailException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .error(ErrorResponse.ErrorDetails.builder()
                        .code("DUPLICATE_RESOURCE")
                        .details(ex.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .requestId(UUID.randomUUID())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(com.drey.eventsSports.domain.model.exceptions.DuplicateUsernameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUsername(com.drey.eventsSports.domain.model.exceptions.DuplicateUsernameException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .error(ErrorResponse.ErrorDetails.builder()
                        .code("DUPLICATE_RESOURCE")
                        .details(ex.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .requestId(UUID.randomUUID())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status("error")
                .error(ErrorResponse.ErrorDetails.builder()
                        .code("INTERNAL_SERVER_ERROR")
                        .details(ex.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .requestId(UUID.randomUUID())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

