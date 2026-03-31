package com.vijayasree.pos.service.impl;

import com.vijayasree.pos.dto.request.LoginRequest;
import com.vijayasree.pos.dto.response.LoginResponse;
import com.vijayasree.pos.entity.Permission;
import com.vijayasree.pos.entity.User;
import com.vijayasree.pos.exceptions.ResourceNotFoundException;
import com.vijayasree.pos.repository.UserRepository;
import com.vijayasree.pos.security.JwtUtil;
import com.vijayasree.pos.security.LoginRateLimiter;
import com.vijayasree.pos.service.AuthService;
import com.vijayasree.pos.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.Now;
import org.aspectj.lang.annotation.After;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter loginRateLimiter;  // ← add this

    @Override
    public LoginResponse login(LoginRequest request) {


        // Check rate limit first
        loginRateLimiter.isAllowed(request.getUsername());

        User user = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid username or password"));

        if (!user.getActive()) {
            throw new IllegalArgumentException(
                    "Account is deactivated. Contact admin.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {

            throw new IllegalArgumentException("Invalid username or password");
        }

        // ← Reset attempts on successful login
        loginRateLimiter.resetAttempts(request.getUsername());package com.vijayasree.pos.service.impl;

import com.vijayasree.pos.dto.request.LoginRequest;
import com.vijayasree.pos.dto.response.LoginResponse;
import com.vijayasree.pos.entity.Permission;
import com.vijayasree.pos.entity.User;
import com.vijayasree.pos.exceptions.ResourceNotFoundException;
import com.vijayasree.pos.repository.UserRepository;
import com.vijayasree.pos.security.JwtUtil;
import com.vijayasree.pos.security.LoginRateLimiter;
import com.vijayasree.pos.service.AuthService;
import com.vijayasree.pos.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.Now;
import org.aspectj.lang.annotation.After;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

        @Service
        @RequiredArgsConstructor
        @Slf4j
        public class AuthServiceImpl implements AuthService {

            private final UserRepository userRepository;
            private final JwtUtil jwtUtil;
            private final PermissionService permissionService;
            private final PasswordEncoder passwordEncoder;
            private final LoginRateLimiter loginRateLimiter;  // ← add this

            @Override
            public LoginResponse login(LoginRequest request) {


                // Check rate limit first
                loginRateLimiter.isAllowed(request.getUsername());

                User user = userRepository.findByUsernameIgnoreCase(request.getUsername())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Invalid username or password"));

                if (!user.getActive()) {
                    throw new IllegalArgumentException(
                            "Account is deactivated. Contact admin.");
                }

                if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {

                    throw new IllegalArgumentException("Invalid username or password");
                }

                // ← Reset attempts on successful login
                loginRateLimiter.resetAttempts(request.getUsername());

                Set<Permission> basePermissions = user.getRole().getPermissions();
                Set<Permission> effectivePermissions = permissionService
                        .resolveEffectivePermissions(user.getId(), basePermissions);

                String permissionsString = effectivePermissions.stream()
                        .map(Permission::name)
                        .collect(Collectors.joining(","));

                String token = jwtUtil.generateToken(
                        user.getUsername(),
                        user.getRole().getName(),
                        permissionsString
                );

                log.info("User logged in: {} ({}) with {} permissions",
                        user.getUsername(), user.getRole().getName(),
                        effectivePermissions.size());

                return LoginResponse.builder()
                        .id(user.getId())
                        .token(token)
                        .username(user.getUsername())
                        .name(user.getName())
                        .role(user.getRole().getName())
                        .roleId(user.getRole().getId())
                        .permissions(effectivePermissions.stream()
                                .map(Permission::name).toList())
                        .build();
            }
        }


        Set<Permission> basePermissions = user.getRole().getPermissions();
        Set<Permission> effectivePermissions = permissionService
                .resolveEffectivePermissions(user.getId(), basePermissions);

        String permissionsString = effectivePermissions.stream()
                .map(Permission::name)
                .collect(Collectors.joining(","));

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().getName(),
                permissionsString
        );

        log.info("User logged in: {} ({}) with {} permissions",
                user.getUsername(), user.getRole().getName(),
                effectivePermissions.size());

        return LoginResponse.builder()
                .id(user.getId())
                .token(token)
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().getName())
                .roleId(user.getRole().getId())
                .permissions(effectivePermissions.stream()
                        .map(Permission::name).toList())
                .build();
    }
}
