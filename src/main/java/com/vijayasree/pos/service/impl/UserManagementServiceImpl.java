package com.vijayasree.pos.service.impl;

import com.vijayasree.pos.dto.request.UserRequest;
import com.vijayasree.pos.dto.response.UserResponse;
import com.vijayasree.pos.entity.CustomRole;
import com.vijayasree.pos.entity.StockAdjustment;
import com.vijayasree.pos.entity.User;
import com.vijayasree.pos.exceptions.ResourceNotFoundException;
import com.vijayasree.pos.repository.*;
import com.vijayasree.pos.service.SaleService;
import com.vijayasree.pos.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final CustomRoleRepository customRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPermissionRepository userPermissionRepository;
    private final SaleRepository saleRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;

    @Override
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        CustomRole role = findRole(request.getRoleId());

        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))  // ← fix here
                .role(role)
                .active(true)
                .build();

        return toResponse(userRepository.save(user));
    }

    @Override
    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse getById(Long id) {
        return toResponse(findUser(id));
    }

    @Override
    public UserResponse update(Long id, UserRequest request) {
        User user = findUser(id);
        CustomRole role = findRole(request.getRoleId());

        user.setName(request.getName());
        user.setRole(role);
        if (!request.getPassword().isBlank()) {
            user.setPasswordHash(request.getPassword());
        }

        return toResponse(userRepository.save(user));
    }

    @Override
    public void deactivate(Long id) {
        User user = findUser(id);
        if (user.getRole().getIsSystem()) {
            throw new IllegalArgumentException("Cannot deactivate a system admin");
        }
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", user.getUsername());
    }

    @Override
    public void activate(Long id) {
        User user = findUser(id);
        user.setActive(true);
        userRepository.save(user);
        log.info("User activated: {}", user.getUsername());
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private CustomRole findRole(Long roleId) {
        return customRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .roleName(user.getRole().getName())
                .roleId(user.getRole().getId())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.isBlank())
            throw new IllegalArgumentException("Password cannot be empty");

        User user = findUser(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset for user: {}", user.getUsername());
    }
    @Override
    public void delete(Long id) {
        User user = findUser(id);

        if (user.getRole().getIsSystem()) {
            throw new IllegalArgumentException("Cannot delete system admin users");
        }

        // Remove user permissions first
        userPermissionRepository.deleteByUserId(id);

        // Set sold_by to null on sales before deleting
        saleRepository.clearSoldBy(id);

        // Set adjusted_by to null on stock adjustments
        stockAdjustmentRepository.clearAdjustedBy(id);

        userRepository.delete(user);
        log.info("User permanently deleted: {}", user.getUsername());
    }
}