package com.drey.eventsSports.application.dtos;

import java.util.List;

public record UserDto(
        Long id,
        String email,
        List<String> roles
) {
}
