package com.vijayasree.pos.controller;

import com.vijayasree.pos.dto.request.PermissionRequest;
import com.vijayasree.pos.dto.response.ApiResponse;
import com.vijayasree.pos.dto.response.UserPermissionResponse;
import com.vijayasree.pos.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('USER_GRANT_PERMISSION')")
    public ResponseEntity<ApiResponse<UserPermissionResponse>> getUserPermissions(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                permissionService.getUserPermissions(userId)));
    }

    @PostMapping("/user/{userId}/grant")
    @PreAuthorize("hasAuthority('USER_GRANT_PERMISSION')")
    public ResponseEntity<ApiResponse<Void>> grant(
            @PathVariable Long userId,
            @Valid @RequestBody PermissionRequest request) {
        permissionService.grantPermission(
                userId, request.getPermission(), request.getAdminId());
        return ResponseEntity.ok(ApiResponse.success(null,
                "Permission " + request.getPermission() + " granted successfully"));
    }

    @PostMapping("/user/{userId}/revoke")
    @PreAuthorize("hasAuthority('USER_GRANT_PERMISSION')")
    public ResponseEntity<ApiResponse<Void>> revoke(
            @PathVariable Long userId,
            @Valid @RequestBody PermissionRequest request) {
        permissionService.revokePermission(
                userId, request.getPermission(), request.getAdminId());
        return ResponseEntity.ok(ApiResponse.success(null,
                "Permission " + request.getPermission() + " revoked successfully"));
    }

    @PostMapping("/user/{userId}/reset")
    @PreAuthorize("hasAuthority('USER_GRANT_PERMISSION')")
    public ResponseEntity<ApiResponse<Void>> reset(@PathVariable Long userId) {
        permissionService.resetToRoleDefaults(userId);
        return ResponseEntity.ok(ApiResponse.success(null,
                "Permissions reset to role defaults"));
    }
}