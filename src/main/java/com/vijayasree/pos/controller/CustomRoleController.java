package com.vijayasree.pos.controller;

import com.vijayasree.pos.dto.request.CustomRoleRequest;
import com.vijayasree.pos.dto.response.ApiResponse;
import com.vijayasree.pos.dto.response.CustomRoleResponse;
import com.vijayasree.pos.entity.Permission;
import com.vijayasree.pos.service.CustomRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomRoleController {

    private final CustomRoleService customRoleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomRoleResponse>> create(
            @Valid @RequestBody CustomRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(customRoleService.create(request), "Role created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CustomRoleResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(customRoleService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomRoleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customRoleService.getById(id)));
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Permission>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.success(customRoleService.getAllPermissions()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomRoleResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                customRoleService.update(id, request), "Role updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customRoleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Role deleted successfully"));
    }
}