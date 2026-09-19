package com.drey.eventsSports.domain.ports.inbound;

import com.drey.eventsSports.domain.model.entities.User;

public interface CreateUserUseCase {
    User createUser(CreateUserCommand command);
}
