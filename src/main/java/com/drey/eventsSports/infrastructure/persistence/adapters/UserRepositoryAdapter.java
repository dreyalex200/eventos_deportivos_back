package com.drey.eventsSports.infrastructure.persistence.adapters;

import com.drey.eventsSports.domain.model.entities.Permission;
import com.drey.eventsSports.domain.model.entities.Role;
import com.drey.eventsSports.domain.model.entities.User;
import com.drey.eventsSports.domain.ports.outbound.UserRepository;
import com.drey.eventsSports.infrastructure.persistence.entities.RoleJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.entities.UserJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataRoleRepository;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataUserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final SpringDataRoleRepository springDataRoleRepository;

    public UserRepositoryAdapter(
            SpringDataUserRepository springDataUserRepository,
            SpringDataRoleRepository springDataRoleRepository) {
        this.springDataUserRepository = springDataUserRepository;
        this.springDataRoleRepository = springDataRoleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return springDataUserRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity savedEntity = springDataUserRepository.save(entity);
        return toDomain(savedEntity);
    }

    private User toDomain(UserJpaEntity entity) {
        Set<Role> roles = new HashSet<>();
        if (entity.getRoles() != null) {
            for (RoleJpaEntity r : entity.getRoles()) {
                Set<Permission> permissions = new HashSet<>();
                if (r.getPermissions() != null) {
                    for (var p : r.getPermissions()) {
                        permissions.add(new Permission(
                                p.getId(),
                                p.getCode(),
                                p.getName(),
                                p.getDescription(),
                                p.getStatus()
                        ));
                    }
                }
                roles.add(new Role(
                        r.getId(),
                        r.getCode(),
                        r.getName(),
                        r.getDescription(),
                        r.getStatus(),
                        permissions
                ));
            }
        }

        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .roles(roles)
                .lastLoginAt(entity.getLastLoginAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private UserJpaEntity toEntity(User user) {
        Set<RoleJpaEntity> roleEntities = new HashSet<>();
        if (user.getRoles() != null) {
            for (Role r : user.getRoles()) {
                Optional<RoleJpaEntity> roleEntityOpt = Optional.empty();
                if (r.getId() != null) {
                    roleEntityOpt = springDataRoleRepository.findById(r.getId());
                }
                if (roleEntityOpt.isEmpty() && r.getCode() != null) {
                    roleEntityOpt = springDataRoleRepository.findByCode(r.getCode());
                }
                roleEntityOpt.ifPresent(roleEntities::add);
            }
        }

        return UserJpaEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .passwordHash(user.getPasswordHash())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roleEntities)
                .build();
    }
}
