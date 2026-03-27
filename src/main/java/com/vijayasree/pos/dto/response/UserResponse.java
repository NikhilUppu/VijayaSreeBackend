package com.vijayasree.pos.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String username;
    private String roleName;
    private Long roleId;
    private Boolean active;
    private LocalDateTime createdAt;
}