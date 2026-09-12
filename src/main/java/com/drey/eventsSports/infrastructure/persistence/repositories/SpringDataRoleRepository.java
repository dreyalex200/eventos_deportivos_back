package com.drey.eventsSports.infrastructure.persistence.repositories;

import com.drey.eventsSports.infrastructure.persistence.entities.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataRoleRepository extends JpaRepository<RoleJpaEntity, Long> {
    Optional<RoleJpaEntity> findByCode(String code);
    boolean existsByCode(String code);
}
