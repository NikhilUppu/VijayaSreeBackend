package com.vijayasree.pos.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustmentRequest {

    @NotNull(message = "New quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer newQty;

    @NotNull(message = "Adjusted by user ID is required")
    private Long adjustedById;

    private String reason;

    private String adjustmentType;
}