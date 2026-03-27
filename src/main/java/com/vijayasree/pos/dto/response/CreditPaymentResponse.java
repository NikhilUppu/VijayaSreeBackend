package com.vijayasree.pos.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CreditPaymentResponse {
    private Long id;
    private BigDecimal amount;
    private String note;
    private String receivedBy;
    private LocalDateTime createdAt;
}