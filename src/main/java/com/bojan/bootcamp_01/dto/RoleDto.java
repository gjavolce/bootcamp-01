package com.bojan.bootcamp_01.dto;

import java.util.UUID;

public class RoleDto {
    private UUID id;
    private String name;

    public RoleDto() {
    }

    public RoleDto(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
