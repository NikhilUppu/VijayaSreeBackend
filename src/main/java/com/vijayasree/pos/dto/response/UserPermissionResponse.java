package com.vijayasree.pos.dto.response;

import com.vijayasree.pos.entity.Permission;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserPermissionResponse {
    private Long userId;
    private String username;
    private String name;
    private String role;
    private Set<Permission> roleDefaultPermissions;
    private List<String> extraGranted;
    private List<String> explicitlyRevoked;
    private Set<Permission> effectivePermissions;
}