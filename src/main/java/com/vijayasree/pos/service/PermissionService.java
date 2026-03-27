package com.vijayasree.pos.service;

import com.vijayasree.pos.dto.response.UserPermissionResponse;
import com.vijayasree.pos.entity.Permission;

import java.util.Set;

public interface PermissionService {
    UserPermissionResponse getUserPermissions(Long userId);
    void grantPermission(Long userId, Permission permission, Long adminId);
    void revokePermission(Long userId, Permission permission, Long adminId);
    void resetToRoleDefaults(Long userId);
    Set<Permission> resolveEffectivePermissions(Long userId, Set<Permission> basePermissions);
}