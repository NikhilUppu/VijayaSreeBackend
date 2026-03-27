package com.vijayasree.pos.service;

import com.vijayasree.pos.dto.request.CustomRoleRequest;
import com.vijayasree.pos.dto.response.CustomRoleResponse;
import com.vijayasree.pos.entity.Permission;

import java.util.List;
import java.util.Set;

public interface CustomRoleService {
    CustomRoleResponse create(CustomRoleRequest request);
    List<CustomRoleResponse> getAll();
    CustomRoleResponse getById(Long id);
    CustomRoleResponse update(Long id, CustomRoleRequest request);
    void delete(Long id);
    List<Permission> getAllPermissions();
}