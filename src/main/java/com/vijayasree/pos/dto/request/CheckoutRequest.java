package com.vijayasree.pos.dto.request;

import com.vijayasree.pos.entity.Sale;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckoutRequest {

    private String customerName;
    private String customerPhone;
    private String customerVillage;
    private Sale.PaymentMethod paymentMethod;

    @NotNull(message = "Sold by user ID is required")
    private Long soldById;

    @NotEmpty(message = "Cart cannot be empty")
    private List<CheckoutItemRequest> items;

    private BigDecimal discountValue;
    private Sale.DiscountType discountType;

    private Boolean isCredit;
}