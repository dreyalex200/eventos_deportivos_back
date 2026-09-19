package com.drey.eventsSports.infrastructure.persistence.repositories;

import com.drey.eventsSports.infrastructure.persistence.entities.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataPermissionRepository extends JpaRepository<PermissionJpaEntity, Long> {
    Optional<PermissionJpaEntity> findByCode(String code);
    boolean existsByCode(String code);
}
