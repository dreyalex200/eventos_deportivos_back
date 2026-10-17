package com.drey.eventsSports.infrastructure.persistence.adapters;

import com.drey.eventsSports.domain.model.entities.SportEvent;
import com.drey.eventsSports.domain.ports.outbound.EventRepository;
import com.drey.eventsSports.infrastructure.persistence.entities.EventJpaEntity;
import com.drey.eventsSports.infrastructure.persistence.repositories.SpringDataEventRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class EventRepositoryAdapter implements EventRepository {

    private final SpringDataEventRepository springDataEventRepository;
    private final ObjectMapper objectMapper;

    public EventRepositoryAdapter(SpringDataEventRepository springDataEventRepository, ObjectMapper objectMapper) {
        this.springDataEventRepository = springDataEventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SportEvent> findById(Long id) {
        return springDataEventRepository.findByIdAndDeletedAtIsNull(id)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SportEvent> findAll(Pageable pageable) {
        return springDataEventRepository.findByDeletedAtIsNull(pageable)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public SportEvent save(SportEvent event) {
        EventJpaEntity entity = toEntity(event);
        EventJpaEntity saved = springDataEventRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        springDataEventRepository.findByIdAndDeletedAtIsNull(id).ifPresent(entity -> {
            entity.setDeletedAt(LocalDateTime.now());
            springDataEventRepository.save(entity);
        });
    }

    private SportEvent toDomain(EventJpaEntity entity) {
        String sportType = resolveSportTypeFromId(entity.getSportId());
        String location = "";
        int maxParticipants = 0;
        int currentParticipants = 0;
        LocalDateTime startDateTime = entity.getStartDate() != null ? entity.getStartDate().atStartOfDay() : null;
        LocalDateTime endDateTime = entity.getEndDate() != null ? entity.getEndDate().atTime(23, 59, 59) : null;

        if (entity.getConfiguration() != null && !entity.getConfiguration().isBlank()) {
            try {
                Map<String, Object> config = objectMapper.readValue(entity.getConfiguration(), new TypeReference<>() {});
                if (config.get("sportType") instanceof String s && !s.isBlank()) {
                    sportType = s;
                }
                if (config.get("location") instanceof String l) {
                    location = l;
                }
                if (config.get("maxParticipants") instanceof Number n) {
                    maxParticipants = n.intValue();
                }
                if (config.get("currentParticipants") instanceof Number n) {
                    currentParticipants = n.intValue();
                }
                if (config.get("startDateTime") instanceof String dt && !dt.isBlank()) {
                    startDateTime = LocalDateTime.parse(dt);
                }
                if (config.get("endDateTime") instanceof String dt && !dt.isBlank()) {
                    endDateTime = LocalDateTime.parse(dt);
                }
            } catch (Exception ignored) {
            }
        }

        return SportEvent.builder()
                .id(entity.getId())
                .title(entity.getName())
                .description(entity.getDescription())
                .sportType(sportType)
                .location(location)
                .startDate(startDateTime)
                .endDate(endDateTime)
                .status(entity.getStatus())
                .maxParticipants(maxParticipants)
                .currentParticipants(currentParticipants)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    private EventJpaEntity toEntity(SportEvent event) {
        Long sportId = resolveSportId(event.getSportType());
        LocalDate startDate = event.getStartDate() != null ? event.getStartDate().toLocalDate() : null;
        LocalDate endDate = event.getEndDate() != null ? event.getEndDate().toLocalDate() : null;

        Map<String, Object> config = new HashMap<>();
        config.put("sportType", event.getSportType());
        config.put("location", event.getLocation());
        config.put("maxParticipants", event.getMaxParticipants());
        config.put("currentParticipants", event.getCurrentParticipants());
        if (event.getStartDate() != null) {
            config.put("startDateTime", event.getStartDate().toString());
        }
        if (event.getEndDate() != null) {
            config.put("endDateTime", event.getEndDate().toString());
        }

        String configJson = null;
        try {
            configJson = objectMapper.writeValueAsString(config);
        } catch (Exception ignored) {
        }

        String code = "EVT-" + (event.getId() != null ? event.getId() : System.currentTimeMillis());

        return EventJpaEntity.builder()
                .id(event.getId())
                .sportId(sportId)
                .competitionTypeId(1L)
                .name(event.getTitle())
                .code(code)
                .description(event.getDescription())
                .startDate(startDate)
                .endDate(endDate)
                .status(event.getStatus() != null ? event.getStatus() : "DRAFT")
                .configuration(configJson)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .deletedAt(event.getDeletedAt())
                .build();
    }

    private Long resolveSportId(String sportType) {
        if (sportType == null) return 1L;
        return switch (sportType.trim().toUpperCase()) {
            case "FOOTBALL" -> 1L;
            case "BASKETBALL" -> 2L;
            case "VOLLEYBALL" -> 3L;
            case "BASEBALL" -> 4L;
            case "FUTSAL" -> 5L;
            default -> 6L; // OTHER
        };
    }

    private String resolveSportTypeFromId(Long sportId) {
        if (sportId == null) return "FOOTBALL";
        return switch (sportId.intValue()) {
            case 1 -> "FOOTBALL";
            case 2 -> "BASKETBALL";
            case 3 -> "VOLLEYBALL";
            case 4 -> "BASEBALL";
            case 5 -> "FUTSAL";
            default -> "OTHER";
        };
    }
}
