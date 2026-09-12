package com.drey.eventsSports.domain.model.exceptions;

public class UnauthorizedRoleException extends RuntimeException {
    public UnauthorizedRoleException(String message) {
        super(message);
    }

    public UnauthorizedRoleException() {
        super("User does not have required administrator privileges");
    }
}
