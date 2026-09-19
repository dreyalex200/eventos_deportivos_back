package com.drey.eventsSports.domain.model.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Role {
    private final Long id;
    private final String code;
    private final String name;
    private final String description;
    private final Integer status;
    private final Set<Permission> permissions;

    public Role(Long id, String code, String name, String description, Integer status, Set<Permission> permissions) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.status = status != null ? status : 1;
        this.permissions = permissions != null ? new HashSet<>(permissions) : new HashSet<>();
    }

    public Role(Long id, String code, String name, String description, Integer status) {
        this(id, code, name, description, status, Collections.emptySet());
    }

    public Role(String code, String name) {
        this(null, code, name, null, 1, Collections.emptySet());
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getStatus() {
        return status;
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public boolean isActive() {
        return status != null && status == 1;
    }

    public boolean hasPermission(String permissionCode) {
        if (permissionCode == null || permissions == null) {
            return false;
        }
        return permissions.stream()
                .filter(Permission::isActive)
                .anyMatch(p -> permissionCode.equalsIgnoreCase(p.getCode()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(code, role.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }

    public static class Builder {
        private Long id;
        private String code;
        private String name;
        private String description;
        private Integer status = 1;
        private Set<Permission> permissions = new HashSet<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
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

        public Builder permissions(Set<Permission> permissions) {
            this.permissions = permissions != null ? permissions : new HashSet<>();
            return this;
        }

        public Builder addPermission(Permission permission) {
            if (this.permissions == null) {
                this.permissions = new HashSet<>();
            }
            this.permissions.add(permission);
            return this;
        }

        public Role build() {
            return new Role(id, code, name, description, status, permissions);
        }
    }
}
