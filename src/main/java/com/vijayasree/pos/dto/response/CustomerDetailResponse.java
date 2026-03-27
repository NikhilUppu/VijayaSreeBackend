package com.vijayasree.pos.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CustomerDetailResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private String village;
    private BigDecimal creditLimit;
    private BigDecimal creditBalance;
    private Boolean active;
    private int totalOrders;
    private BigDecimal totalSpent;
    private LocalDateTime lastVisit;
    private LocalDateTime createdAt;
    private List<SaleResponse> recentSales;
    private List<CreditPaymentResponse> creditPayments;
}