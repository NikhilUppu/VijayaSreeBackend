package com.vijayasree.pos.dto.response;

import com.vijayasree.pos.entity.Sale;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SaleResponse {
    private Long id;
    private String receiptNo;
    private String customerName;
    private String customerPhone;
    private String customerVillage;
    private Long customerId;
    private String soldBy;
    private Sale.PaymentMethod paymentMethod;
    private List<SaleItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal discountValue;
    private Sale.DiscountType discountType;
    private BigDecimal grandTotal;
    private Boolean isCredit;
    private LocalDateTime createdAt;
}