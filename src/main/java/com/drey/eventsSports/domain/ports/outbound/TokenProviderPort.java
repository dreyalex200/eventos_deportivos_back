package com.drey.eventsSports.domain.ports.outbound;

import com.drey.eventsSports.domain.model.entities.User;

public interface TokenProviderPort {
    String generateToken(User user);
    String generateToken(User user, long expirationSeconds);
    long getExpirationSeconds();
}
