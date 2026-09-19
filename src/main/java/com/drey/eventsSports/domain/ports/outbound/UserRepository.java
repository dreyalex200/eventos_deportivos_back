package com.drey.eventsSports.domain.ports.outbound;

import com.drey.eventsSports.domain.model.entities.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    User save(User user);
}
