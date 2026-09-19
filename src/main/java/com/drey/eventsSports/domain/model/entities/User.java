package com.drey.eventsSports.domain.model.entities;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class User {
    private final Long id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final Integer status;
    private final Set<Role> roles;
    private final LocalDateTime lastLoginAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public User(Long id, String username, String email, String passwordHash,
                String firstName, String lastName, String phone, Integer status,
                Set<Role> roles, LocalDateTime lastLoginAt,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.status = status != null ? status : 1;
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public Integer getStatus() {
        return status;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isActive() {
        return status != null && status == 1;
    }

    public boolean hasRole(String roleCode) {
        if (roleCode == null || roles == null) {
            return false;
        }
        return roles.stream()
                .filter(Role::isActive)
                .anyMatch(r -> roleCode.equalsIgnoreCase(r.getCode()));
    }

    public Set<String> getEffectivePermissions() {
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles.stream()
                .filter(Role::isActive)
                .flatMap(r -> r.getPermissions().stream())
                .filter(Permission::isActive)
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    public boolean hasPermission(String permissionCode) {
        if (permissionCode == null || roles == null) {
            return false;
        }
        return roles.stream()
                .filter(Role::isActive)
                .anyMatch(r -> r.hasPermission(permissionCode));
    }

    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private String passwordHash;
        private String firstName;
        private String lastName;
        private String phone;
        private Integer status = 1;
        private Set<Role> roles = new HashSet<>();
        private LocalDateTime lastLoginAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder status(String statusStr) {
            if ("active".equalsIgnoreCase(statusStr)) {
                this.status = 1;
            } else if ("inactive".equalsIgnoreCase(statusStr)) {
                this.status = 0;
            } else {
                try {
                    this.status = Integer.parseInt(statusStr);
                } catch (NumberFormatException ignored) {
                    this.status = 1;
                }
            }
            return this;
        }

        public Builder roles(Set<Role> roles) {
            this.roles = roles != null ? roles : new HashSet<>();
            return this;
        }

        public Builder addRole(Role role) {
            if (this.roles == null) {
                this.roles = new HashSet<>();
            }
            this.roles.add(role);
            return this;
        }

        public Builder lastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
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

        public User build() {
            return new User(id, username, email, passwordHash, firstName, lastName, phone, status, roles, lastLoginAt, createdAt, updatedAt);
        }
    }
}
