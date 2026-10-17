package com.drey.eventsSports.infrastructure.persistence.repositories;

import com.drey.eventsSports.infrastructure.persistence.entities.EventJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, Long> {
    Page<EventJpaEntity> findByDeletedAtIsNull(Pageable pageable);
    Optional<EventJpaEntity> findByIdAndDeletedAtIsNull(Long id);
}
