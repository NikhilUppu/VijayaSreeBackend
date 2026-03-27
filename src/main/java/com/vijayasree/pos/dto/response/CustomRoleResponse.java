package com.vijayasree.pos.dto.response;

import com.vijayasree.pos.entity.Permission;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class CustomRoleResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean isSystem;
    private Set<Permission> permissions;
    private long userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}