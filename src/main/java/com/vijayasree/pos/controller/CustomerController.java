package com.vijayasree.pos.controller;

import com.vijayasree.pos.dto.request.CreditPaymentRequest;
import com.vijayasree.pos.dto.request.CustomerRequest;
import com.vijayasree.pos.dto.response.ApiResponse;
import com.vijayasree.pos.dto.response.CustomerDetailResponse;
import com.vijayasree.pos.dto.response.CustomerResponse;
import com.vijayasree.pos.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAuthority('SALES_CHECKOUT')")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        customerService.create(request), "Customer created"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_CHECKOUT')")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAll()));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('SALES_CHECKOUT')")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> search(
            @RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.success(customerService.search(query)));
    }

    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAuthority('SALES_CHECKOUT')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getByPhone(
            @PathVariable String phone) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getByPhone(phone)));
    }

    @GetMapping("/credit")
    @PreAuthorize("hasAuthority('REPORTS_DAILY')")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getCreditCustomers() {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.getCreditCustomers()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_CHECKOUT')")
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_CHECKOUT')")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.update(id, request), "Customer updated"));
    }

    @PostMapping("/{id}/credit-payment")
    @PreAuthorize("hasAuthority('REPORTS_DAILY')")
    public ResponseEntity<ApiResponse<Void>> recordCreditPayment(
            @PathVariable Long id,
            @Valid @RequestBody CreditPaymentRequest request) {
        customerService.recordCreditPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment recorded"));
    }
}