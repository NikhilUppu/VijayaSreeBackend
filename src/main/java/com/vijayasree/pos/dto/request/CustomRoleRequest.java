package com.vijayasree.pos.dto.request;

import com.vijayasree.pos.entity.Permission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class CustomRoleRequest {

    @NotBlank(message = "Role name is required")
    private String name;

    private String description;

    @NotNull(message = "Permissions are required")
    private Set<Permission> permissions;
}