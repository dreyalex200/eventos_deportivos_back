package com.drey.eventsSports.domain.ports.inbound;

import com.drey.eventsSports.domain.model.entities.User;

public record LoginResult(
        String accessToken,
        String tokenType,
        long expiresIn,
        User user
) {
}
