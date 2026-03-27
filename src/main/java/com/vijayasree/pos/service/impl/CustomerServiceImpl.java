package com.vijayasree.pos.service.impl;

import com.vijayasree.pos.dto.request.CreditPaymentRequest;
import com.vijayasree.pos.dto.request.CustomerRequest;
import com.vijayasree.pos.dto.response.CreditPaymentResponse;
import com.vijayasree.pos.dto.response.CustomerDetailResponse;
import com.vijayasree.pos.dto.response.CustomerResponse;
import com.vijayasree.pos.dto.response.SaleResponse;
import com.vijayasree.pos.entity.CreditPayment;
import com.vijayasree.pos.entity.Customer;
import com.vijayasree.pos.entity.Sale;
import com.vijayasree.pos.entity.User;
import com.vijayasree.pos.exceptions.ResourceNotFoundException;
import com.vijayasree.pos.repository.CreditPaymentRepository;
import com.vijayasree.pos.repository.CustomerRepository;
import com.vijayasree.pos.repository.SaleRepository;
import com.vijayasree.pos.repository.UserRepository;
import com.vijayasree.pos.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CreditPaymentRepository creditPaymentRepository;
    private final UserRepository userRepository;
    private final SaleRepository saleRepository;

    @Override
    public CustomerResponse create(CustomerRequest request) {
        if (request.getPhone() != null &&
                customerRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException(
                    "Customer with phone " + request.getPhone() + " already exists");
        }
        Customer customer = Customer.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .village(request.getVillage())
                .creditLimit(request.getCreditLimit() != null
                        ? request.getCreditLimit() : BigDecimal.ZERO)
                .creditBalance(BigDecimal.ZERO)
                .active(true)
                .build();
        return toResponse(customerRepository.save(customer));
    }

    @Override
    public List<CustomerResponse> getAll() {
        return customerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerResponse> search(String query) {
        return customerRepository.searchCustomers(query)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerResponse> getCreditCustomers() {
        return customerRepository.findCreditCustomers()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponse getByPhone(String phone) {
        return customerRepository.findByPhone(phone)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No customer found with phone: " + phone));
    }

    @Override
    public CustomerDetailResponse getById(Long id) {
        Customer customer = findById(id);
        List<Sale> sales = saleRepository.findByCustomerIdOrderByCreatedAtDesc(id);
        List<CreditPayment> payments =
                creditPaymentRepository.findByCustomerIdOrderByCreatedAtDesc(id);

        BigDecimal totalSpent = sales.stream()
                .map(Sale::getGrandTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CustomerDetailResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .village(customer.getVillage())
                .creditLimit(customer.getCreditLimit())
                .creditBalance(customer.getCreditBalance())
                .active(customer.getActive())
                .totalOrders(sales.size())
                .totalSpent(totalSpent)
                .lastVisit(sales.isEmpty() ? null : sales.get(0).getCreatedAt())
                .createdAt(customer.getCreatedAt())
                .recentSales(sales.stream().limit(10)
                        .map(this::toSaleResponse)
                        .collect(Collectors.toList()))
                .creditPayments(payments.stream()
                        .map(p -> CreditPaymentResponse.builder()
                                .id(p.getId())
                                .amount(p.getAmount())
                                .note(p.getNote())
                                .receivedBy(p.getReceivedBy().getName())
                                .createdAt(p.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findById(id);
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setVillage(request.getVillage());
        if (request.getCreditLimit() != null) {
            customer.setCreditLimit(request.getCreditLimit());
        }
        return toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public void recordCreditPayment(Long customerId, CreditPaymentRequest request) {
        Customer customer = findById(customerId);
        User receivedBy = userRepository.findById(request.getReceivedById())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + request.getReceivedById()));

        if (request.getAmount().compareTo(customer.getCreditBalance()) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount exceeds credit balance of ₹" + customer.getCreditBalance());
        }

        customer.setCreditBalance(
                customer.getCreditBalance().subtract(request.getAmount()));
        customerRepository.save(customer);

        creditPaymentRepository.save(CreditPayment.builder()
                .customer(customer)
                .amount(request.getAmount())
                .note(request.getNote())
                .receivedBy(receivedBy)
                .build());

        log.info("Credit payment of ₹{} recorded for customer {}",
                request.getAmount(), customer.getName());
    }

    private Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + id));
    }

    private CustomerResponse toResponse(Customer c) {
        List<Sale> sales = saleRepository.findByCustomerIdOrderByCreatedAtDesc(c.getId());
        BigDecimal totalSpent = sales.stream()
                .map(Sale::getGrandTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CustomerResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .phone(c.getPhone())
                .address(c.getAddress())
                .village(c.getVillage())
                .creditLimit(c.getCreditLimit())
                .creditBalance(c.getCreditBalance())
                .active(c.getActive())
                .totalOrders(sales.size())
                .totalSpent(totalSpent)
                .lastVisit(sales.isEmpty() ? null : sales.get(0).getCreatedAt())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private SaleResponse toSaleResponse(Sale sale) {
        return SaleResponse.builder()
                .id(sale.getId())
                .receiptNo(sale.getReceiptNo())
                .customerName(sale.getCustomerName())
                .grandTotal(sale.getGrandTotal())
                .createdAt(sale.getCreatedAt())
                .build();
    }
}