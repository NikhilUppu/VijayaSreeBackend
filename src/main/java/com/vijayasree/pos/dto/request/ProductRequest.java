package com.vijayasree.pos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;



    @NotBlank(message = "SKU is required")
    private String sku;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQty;

    @NotNull(message = "Low stock threshold is required")
    @Min(value = 1, message = "Low stock threshold must be at least 1")
    private Integer lowStockThreshold;

    private String company;

    private String notes;

    private String imageUrl;
}