package com.vijayasree.pos.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditPaymentRequest {

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String note;

    @NotNull(message = "Received by user ID is required")
    private Long receivedById;
}