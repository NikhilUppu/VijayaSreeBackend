package com.vijayasree.pos.controller;

import com.vijayasree.pos.dto.request.UserRequest;
import com.vijayasree.pos.dto.response.ApiResponse;
import com.vijayasree.pos.dto.response.UserResponse;
import com.vijayasree.pos.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserManagementService userManagementService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        userManagementService.create(request), "User created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userManagementService.update(id, request), "User updated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('USER_DEACTIVATE')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        userManagementService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated"));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('USER_DEACTIVATE')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        userManagementService.activate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User activated"));
    }

    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        userManagementService.resetPassword(id, body.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DEACTIVATE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userManagementService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted"));
    }
}