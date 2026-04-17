package com.vijayasree.pos.controller;

import com.vijayasree.pos.dto.request.LoginRequest;
import com.vijayasree.pos.dto.response.ApiResponse;
import com.vijayasree.pos.dto.response.LoginResponse;
import com.vijayasree.pos.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.login(request), "Login successful"));

    }

}