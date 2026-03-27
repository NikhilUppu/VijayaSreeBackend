package com.vijayasree.pos.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DailyReportResponse {
    private String date;
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private Integer totalItemsSold;
    private List<SaleResponse> transactions;
}