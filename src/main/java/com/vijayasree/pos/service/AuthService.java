package com.vijayasree.pos.service;

import com.vijayasree.pos.dto.request.LoginRequest;
import com.vijayasree.pos.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}