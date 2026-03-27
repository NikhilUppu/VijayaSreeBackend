package com.vijayasree.pos.service.impl;

import com.vijayasree.pos.dto.response.UserPermissionResponse;
import com.vijayasree.pos.entity.Permission;
import com.vijayasree.pos.entity.User;
import com.vijayasree.pos.entity.UserPermission;
import com.vijayasree.pos.exceptions.ResourceNotFoundException;
import com.vijayasree.pos.repository.UserPermissionRepository;
import com.vijayasree.pos.repository.UserRepository;
import com.vijayasree.pos.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;

    @Override
    public Set<Permission> resolveEffectivePermissions(Long userId, Set<Permission> basePermissions) {
        Set<Permission> effective = basePermissions.isEmpty()
                ? EnumSet.noneOf(Permission.class)
                : EnumSet.copyOf(basePermissions);

        List<UserPermission> overrides = userPermissionRepository.findByUserId(userId);
        for (UserPermission override : overrides) {
            if (override.getGrantType() == UserPermission.GrantType.GRANT) {
                effective.add(override.getPermission());
            } else {
                effective.remove(override.getPermission());
            }
        }
        return effective;
    }

    @Override
    public UserPermissionResponse getUserPermissions(Long userId) {
        User user = findUser(userId);
        Set<Permission> roleDefaults = user.getRole().getPermissions();
        Set<Permission> effective = resolveEffectivePermissions(userId, roleDefaults);
        List<UserPermission> grants = userPermissionRepository.findGrantedByUserId(userId);
        List<UserPermission> revokes = userPermissionRepository.findRevokedByUserId(userId);

        return UserPermissionResponse.builder()
                .userId(userId)
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().getName())
                .roleDefaultPermissions(roleDefaults)
                .extraGranted(grants.stream().map(g -> g.getPermission().name()).toList())
                .explicitlyRevoked(revokes.stream().map(r -> r.getPermission().name()).toList())
                .effectivePermissions(effective)
                .build();
    }

    @Override
    @Transactional
    public void grantPermission(Long userId, Permission permission, Long adminId) {
        User user = findUser(userId);
        User admin = findUser(adminId);

        if (user.getRole().getIsSystem()) {
            throw new IllegalArgumentException("Cannot modify system admin permissions");
        }

        userPermissionRepository.findByUserIdAndPermission(userId, permission)
                .ifPresent(userPermissionRepository::delete);

        userPermissionRepository.save(UserPermission.builder()
                .user(user)
                .permission(permission)
                .grantType(UserPermission.GrantType.GRANT)
                .grantedBy(admin)
                .build());

        log.info("Admin {} granted {} to {}", admin.getUsername(), permission, user.getUsername());
    }

    @Override
    @Transactional
    public void revokePermission(Long userId, Permission permission, Long adminId) {
        User user = findUser(userId);
        User admin = findUser(adminId);

        if (user.getRole().getIsSystem()) {
            throw new IllegalArgumentException("Cannot modify system admin permissions");
        }

        userPermissionRepository.findByUserIdAndPermission(userId, permission)
                .ifPresent(userPermissionRepository::delete);

        userPermissionRepository.save(UserPermission.builder()
                .user(user)
                .permission(permission)
                .grantType(UserPermission.GrantType.REVOKE)
                .grantedBy(admin)
                .build());

        log.info("Admin {} revoked {} from {}", admin.getUsername(), permission, user.getUsername());
    }

    @Override
    @Transactional
    public void resetToRoleDefaults(Long userId) {
        userPermissionRepository.findByUserId(userId)
                .forEach(userPermissionRepository::delete);
        log.info("Reset permissions for user {} to role defaults", userId);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}