package com.vijayasree.pos.service.impl;

import com.vijayasree.pos.dto.request.CustomRoleRequest;
import com.vijayasree.pos.dto.response.CustomRoleResponse;
import com.vijayasree.pos.entity.CustomRole;
import com.vijayasree.pos.entity.Permission;
import com.vijayasree.pos.exceptions.ResourceNotFoundException;
import com.vijayasree.pos.repository.CustomRoleRepository;
import com.vijayasree.pos.service.CustomRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomRoleServiceImpl implements CustomRoleService {

    private final CustomRoleRepository customRoleRepository;

    @Override
    public CustomRoleResponse create(CustomRoleRequest request) {
        if (customRoleRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Role already exists: " + request.getName());
        }

        Set<Permission> permissions = new HashSet<>(request.getPermissions());

        CustomRole role = CustomRole.builder()
                .name(request.getName())
                .description(request.getDescription())
                .permissions(permissions)
                .isSystem(false)
                .build();

        return toResponse(customRoleRepository.save(role));
    }

    @Override
    public List<CustomRoleResponse> getAll() {
        return customRoleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CustomRoleResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional
    public CustomRoleResponse update(Long id, CustomRoleRequest request) {
        CustomRole role = findById(id);

        if (role.getIsSystem() && !role.getName().equalsIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Cannot rename a system role");
        }

        Set<Permission> permissions = new HashSet<>(request.getPermissions());

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(permissions);

        log.info("Role updated: {} with {} permissions",
                role.getName(), permissions.size());
        return toResponse(customRoleRepository.save(role));
    }

    @Override
    public void delete(Long id) {
        CustomRole role = findById(id);

        if (role.getIsSystem()) {
            throw new IllegalArgumentException(
                    "Cannot delete system role: " + role.getName());
        }

        long userCount = customRoleRepository.countUsersWithRole(id);
        if (userCount > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete role — " + userCount +
                            " user(s) assigned. Reassign them first.");
        }

        customRoleRepository.delete(role);
        log.info("Role deleted: {}", role.getName());
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Arrays.asList(Permission.values());
    }

    private CustomRole findById(Long id) {
        return customRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + id));
    }

    private CustomRoleResponse toResponse(CustomRole role) {
        return CustomRoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(role.getIsSystem())
                .permissions(role.getPermissions())
                .userCount(customRoleRepository.countUsersWithRole(role.getId()))
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}