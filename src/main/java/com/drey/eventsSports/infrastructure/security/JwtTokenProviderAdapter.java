package com.drey.eventsSports.infrastructure.security;

import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.ports.outbound.TokenProviderPort;
import com.drey.eventsSports.shared.utils.JwtUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    private final JwtUtils jwtUtils;

    public JwtTokenProviderAdapter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public String generateToken(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        List<String> permissions = new ArrayList<>(user.getEffectivePermissions());

        return jwtUtils.generateToken(user.getId(), user.getEmail(), user.getUsername(), roles, permissions);
    }

    @Override
    public String generateToken(User user, long expirationSeconds) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        List<String> permissions = new ArrayList<>(user.getEffectivePermissions());

        return jwtUtils.generateToken(user.getId(), user.getEmail(), user.getUsername(), roles, permissions, expirationSeconds);
    }

    @Override
    public long getExpirationSeconds() {
        return jwtUtils.getExpirationSeconds();
    }
}
