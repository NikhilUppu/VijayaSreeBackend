package com.vijayasree.pos.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String sku;
    private String categoryName;
    private String company;
    private BigDecimal price;
    private Integer stockQty;
    private Integer lowStockThreshold;
    private String notes;
    private Boolean active;
    private String stockStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imageUrl;
}