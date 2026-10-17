package com.drey.eventsSports.domain.model.entities;

import java.time.LocalDateTime;

public class SportEvent {
    private final Long id;
    private final String title;
    private final String description;
    private final String sportType;
    private final String location;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String status;
    private final Integer maxParticipants;
    private final Integer currentParticipants;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public SportEvent(Long id, String title, String description, String sportType,
                      String location, LocalDateTime startDate, LocalDateTime endDate,
                      String status, Integer maxParticipants, Integer currentParticipants,
                      LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.sportType = sportType;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status != null ? status : "DRAFT";
        this.maxParticipants = maxParticipants != null ? maxParticipants : 0;
        this.currentParticipants = currentParticipants != null ? currentParticipants : 0;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSportType() {
        return sportType;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public String getStatus() {
        return status;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private String sportType = "FOOTBALL";
        private String location;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String status = "DRAFT";
        private Integer maxParticipants = 0;
        private Integer currentParticipants = 0;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder sportType(String sportType) {
            this.sportType = sportType;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder maxParticipants(Integer maxParticipants) {
            this.maxParticipants = maxParticipants;
            return this;
        }

        public Builder currentParticipants(Integer currentParticipants) {
            this.currentParticipants = currentParticipants;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder deletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public SportEvent build() {
            return new SportEvent(id, title, description, sportType, location, startDate, endDate,
                    status, maxParticipants, currentParticipants, createdAt, updatedAt, deletedAt);
        }
    }
}
