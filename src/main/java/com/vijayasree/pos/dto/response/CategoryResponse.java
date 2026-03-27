package com.vijayasree.pos.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}