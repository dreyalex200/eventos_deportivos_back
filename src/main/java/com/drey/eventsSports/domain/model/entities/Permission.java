package com.drey.eventsSports.domain.model.entities;

import java.util.Objects;

public class Permission {
    private final Long id;
    private final String code;
    private final String name;
    private final String description;
    private final Integer status;

    public Permission(Long id, String code, String name, String description, Integer status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.status = status != null ? status : 1;
    }

    public Permission(String code, String name) {
        this(null, code, name, null, 1);
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

    public boolean isActive() {
        return status != null && status == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
