package com.drey.eventsSports.domain.ports.outbound;

import com.drey.eventsSports.domain.model.entities.Role;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface RoleRepositoryPort {
    Optional<Role> findByCode(String code);
    Set<Role> findAllByCodes(Collection<String> codes);
}
