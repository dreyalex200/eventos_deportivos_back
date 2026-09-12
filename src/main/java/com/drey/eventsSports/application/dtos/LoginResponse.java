package com.drey.eventsSports.application.dtos;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserDto user
) {
}
