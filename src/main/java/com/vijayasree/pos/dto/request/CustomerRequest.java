package com.vijayasree.pos.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerRequest {

    @NotBlank(message = "Customer name is required")
    private String name;

    private String phone;
    private String address;
    private String village;  // ← add this
    private BigDecimal creditLimit;
}