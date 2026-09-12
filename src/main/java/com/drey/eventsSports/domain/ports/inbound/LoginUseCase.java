package com.drey.eventsSports.domain.ports.inbound;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
