package com.vijayasree.pos.dto.request;

import com.vijayasree.pos.entity.Permission;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionRequest {

    @NotNull(message = "Permission is required")
    private Permission permission;

    @NotNull(message = "Admin ID is required")
    private Long adminId;
}