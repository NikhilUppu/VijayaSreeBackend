package com.vijayasree.pos.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String village;  // ← add this
    private BigDecimal creditLimit;
    private BigDecimal creditBalance;
    private Boolean active;
    private int totalOrders;
    private BigDecimal totalSpent;
    private LocalDateTime lastVisit;
    private LocalDateTime createdAt;
}