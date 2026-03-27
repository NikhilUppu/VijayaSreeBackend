package com.vijayasree.pos.service;

import com.vijayasree.pos.dto.request.CheckoutRequest;
import com.vijayasree.pos.dto.response.DailyReportResponse;
import com.vijayasree.pos.dto.response.SaleResponse;

import java.time.LocalDate;
import java.util.List;

public interface SaleService {
    SaleResponse checkout(CheckoutRequest request);
    DailyReportResponse getDailyReport(LocalDate date);
    List<SaleResponse> getAll();
    SaleResponse getById(Long id);
}