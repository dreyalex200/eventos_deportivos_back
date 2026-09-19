package com.drey.eventsSports.domain.ports.inbound;

import java.util.Set;

public record CreateUserCommand(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        String phone,
        Set<String> roles
) {
}
