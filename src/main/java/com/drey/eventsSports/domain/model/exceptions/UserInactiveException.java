package com.drey.eventsSports.domain.model.exceptions;

public class UserInactiveException extends RuntimeException {
    public UserInactiveException(String message) {
        super(message);
    }

    public UserInactiveException() {
        super("User account is inactive");
    }
}
