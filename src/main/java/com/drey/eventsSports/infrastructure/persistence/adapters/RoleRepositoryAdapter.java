package com.drey.eventsSports.infrastructure.persistence.adapters;

import com.drey.eventsSports.domain.model.entities.Permission;
import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.ports.outbound.RoleRepositoryPort;
import com.drey.eventsSports.infrastructure.persistence.entities.RoleJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataRoleRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final SpringDataRoleRepository springDataRoleRepository;

    public RoleRepositoryAdapter(SpringDataRoleRepository springDataRoleRepository) {
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByCode(String code) {
        return springDataRoleRepository.findByCode(code)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Role> findAllByCodes(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Set.of();
        }
        return springDataRoleRepository.findByCodeIn(codes).stream()
                .map(this::toDomain)
                .collect(Collectors.toSet());
    }

    private Role toDomain(RoleJpaEntity entity) {
        Set<Permission> permissions = new HashSet<>();
        if (entity.getPermissions() != null) {
            for (var p : entity.getPermissions()) {
                permissions.add(new Permission(
                        p.getId(),
                        p.getCode(),
                        p.getName(),
                        p.getDescription(),
                        p.getStatus()
                ));
            }
        }
        return new Role(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                permissions
        );
    }
}
