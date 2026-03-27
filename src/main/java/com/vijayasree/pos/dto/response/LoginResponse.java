package com.vijayasree.pos.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {
    private Long id;
    private String token;
    private String username;
    private String name;
    private String role;
    private Long roleId;
    private List<String> permissions;
}