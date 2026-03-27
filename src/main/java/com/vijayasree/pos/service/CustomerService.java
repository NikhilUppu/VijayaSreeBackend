package com.vijayasree.pos.service;

import com.vijayasree.pos.dto.request.CreditPaymentRequest;
import com.vijayasree.pos.dto.request.CustomerRequest;
import com.vijayasree.pos.dto.response.CustomerDetailResponse;
import com.vijayasree.pos.dto.response.CustomerResponse;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    CustomerResponse create(CustomerRequest request);
    List<CustomerResponse> getAll();
    List<CustomerResponse> search(String query);
    List<CustomerResponse> getCreditCustomers();
    CustomerResponse getByPhone(String phone);
    CustomerDetailResponse getById(Long id);
    CustomerResponse update(Long id, CustomerRequest request);
    void recordCreditPayment(Long customerId, CreditPaymentRequest request);
}