package com.vijayasree.pos.controller;

import com.vijayasree.pos.dto.request.CheckoutRequest;
import com.vijayasree.pos.dto.response.DailyReportResponse;
import com.vijayasree.pos.dto.response.SaleResponse;
import com.vijayasree.pos.service.PrintSseService;
import com.vijayasree.pos.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.vijayasree.pos.dto.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SaleController {

    private final SaleService saleService;
    private final PrintSseService printSseService;

    @PostMapping("/checkout")
    public ResponseEntity<SaleResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        SaleResponse sale = saleService.checkout(request);
        // Broadcast AFTER the transaction commits so Print Station reads a saved sale
        printSseService.broadcast(sale);
        return ResponseEntity.status(HttpStatus.CREATED).body(sale);
    }

    // Print Station calls this on reconnect to fetch bills it missed while offline
    @PreAuthorize("hasAuthority('PRINT_STATION')")
    @GetMapping("/unprinted")
    public ResponseEntity<List<SaleResponse>> getUnprinted() {
        return ResponseEntity.ok(saleService.getUnprinted());
    }

    // Print Station calls this after successfully printing a bill
    @PreAuthorize("hasAuthority('PRINT_STATION')")
    @PostMapping("/{id}/mark-printed")
    public ResponseEntity<Void> markPrinted(@PathVariable Long id) {
        saleService.markPrinted(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<SaleResponse>> getAll() {
        return ResponseEntity.ok(saleService.getAll());
    }

    @GetMapping("/report/daily")
    public ResponseEntity<DailyReportResponse> getDailyReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) date = LocalDate.now();
        return ResponseEntity.ok(saleService.getDailyReport(date));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('REPORTS_DAILY', 'REPORTS_TRANSACTIONS')")
    public ResponseEntity<ApiResponse<SaleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getById(id)));
    }
}