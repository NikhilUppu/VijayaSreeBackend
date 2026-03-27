package com.vijayasree.pos.service;

import com.vijayasree.pos.dto.request.UserRequest;
import com.vijayasree.pos.dto.response.UserResponse;

import java.util.List;

public interface UserManagementService {
    UserResponse create(UserRequest request);
    List<UserResponse> getAll();
    UserResponse getById(Long id);
    UserResponse update(Long id, UserRequest request);
    void deactivate(Long id);
    void activate(Long id);
    void resetPassword(Long id, String newPassword);
    void delete(Long id);
}