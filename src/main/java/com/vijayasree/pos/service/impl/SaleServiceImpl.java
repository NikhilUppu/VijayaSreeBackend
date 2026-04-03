package com.vijayasree.pos.service.impl;

import com.vijayasree.pos.dto.request.CheckoutItemRequest;
import com.vijayasree.pos.dto.request.CheckoutRequest;
import com.vijayasree.pos.dto.response.DailyReportResponse;
import com.vijayasree.pos.dto.response.SaleItemResponse;
import com.vijayasree.pos.dto.response.SaleResponse;
import com.vijayasree.pos.entity.*;
import com.vijayasree.pos.exceptions.InsufficientStockException;
import com.vijayasree.pos.exceptions.ResourceNotFoundException;
import com.vijayasree.pos.repository.CustomerRepository;
import com.vijayasree.pos.repository.ProductRepository;
import com.vijayasree.pos.repository.SaleRepository;
import com.vijayasree.pos.repository.UserRepository;
import com.vijayasree.pos.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public SaleResponse checkout(CheckoutRequest request) {

        // Extract logged-in user from JWT token — no soldById needed from frontend
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User soldBy = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Logged-in user not found: " + username));

        // Find or create customer
        Customer customer = null;
        if (request.getCustomerPhone() != null &&
                !request.getCustomerPhone().isBlank()) {
            customer = customerRepository.findByPhone(request.getCustomerPhone())
                    .orElseGet(() -> {
                        Customer newCust = Customer.builder()
                                .name(request.getCustomerName() != null
                                        ? request.getCustomerName() : "Walk-in")
                                .phone(request.getCustomerPhone())
                                .village(request.getCustomerVillage())
                                .creditBalance(BigDecimal.ZERO)
                                .creditLimit(BigDecimal.ZERO)
                                .active(true)
                                .build();
                        return customerRepository.save(newCust);
                    });

            // Update name if provided
            if (request.getCustomerName() != null &&
                    !request.getCustomerName().isBlank()) {
                customer.setName(request.getCustomerName());
                customer = customerRepository.save(customer);
            }
        }

        List<SaleItem> saleItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CheckoutItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + itemRequest.getProductId()));

            if (product.getStockQty() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for: " + product.getName() +
                                ". Available: " + product.getStockQty() +
                                ", Requested: " + itemRequest.getQuantity());
            }

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            product.setStockQty(product.getStockQty() - itemRequest.getQuantity());
            productRepository.save(product);

            saleItems.add(SaleItem.builder()
                    .productName(product.getName())
                    .product(product)
                    .unitPrice(product.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .lineTotal(lineTotal)
                    .build());
        }

        BigDecimal discountAmount = resolveDiscount(subtotal, request);
        BigDecimal grandTotal = subtotal.subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);

        // Handle credit
        boolean isCredit = Boolean.TRUE.equals(request.getIsCredit());
        if (isCredit && customer != null) {
            if (grandTotal.compareTo(
                    customer.getCreditLimit().subtract(customer.getCreditBalance())) > 0) {
                throw new IllegalArgumentException(
                        "Credit limit exceeded. Available credit: ₹" +
                                customer.getCreditLimit().subtract(customer.getCreditBalance()));
            }
            customer.setCreditBalance(customer.getCreditBalance().add(grandTotal));
            customerRepository.save(customer);
        }

        final Customer finalCustomer = customer;
        Sale sale = Sale.builder()
                .soldBy(soldBy)
                .customer(finalCustomer)
                .receiptNo(generateReceiptNo())
                .customerName(finalCustomer != null
                        ? finalCustomer.getName()
                        : request.getCustomerName() != null
                        ? request.getCustomerName() : "Walk-in Customer")
                .customerPhone(request.getCustomerPhone())
                .customerVillage(request.getCustomerVillage())
                .paymentMethod(request.getPaymentMethod() != null
                        ? request.getPaymentMethod() : Sale.PaymentMethod.CASH)
                .subtotal(subtotal)
                .discountValue(request.getDiscountValue())
                .discountType(request.getDiscountType())
                .grandTotal(grandTotal)
                .creditSale(Boolean.TRUE.equals(request.getIsCredit()))
                .build();

        saleItems.forEach(item -> item.setSale(sale));
        sale.setSaleItems(saleItems);

        Sale saved = saleRepository.save(sale);
        return toResponse(saved);
    }

    @Override
    public DailyReportResponse getDailyReport(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<Sale> sales = saleRepository.findByCreatedAtBetween(start, end);
        BigDecimal revenue = saleRepository.sumRevenueByDateRange(start, end);
        int totalItems = sales.stream()
                .flatMap(s -> s.getSaleItems().stream())
                .mapToInt(SaleItem::getQuantity)
                .sum();

        return DailyReportResponse.builder()
                .date(date.toString())
                .totalRevenue(revenue)
                .totalOrders(sales.size())
                .totalItemsSold(totalItems)
                .transactions(sales.stream().map(this::toResponse).collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<SaleResponse> getAll() {
        return saleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private BigDecimal resolveDiscount(BigDecimal subtotal, CheckoutRequest request) {
        if (request.getDiscountValue() == null || request.getDiscountType() == null) {
            return BigDecimal.ZERO;
        }
        if (request.getDiscountType() == Sale.DiscountType.PERCENTAGE) {
            return subtotal.multiply(request.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return request.getDiscountValue().min(subtotal);
    }

    private String generateReceiptNo() {
        String datePart = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = saleRepository.countByCreatedAtBetween(
                LocalDate.now().atStartOfDay(),
                LocalDate.now().atTime(23, 59, 59)
        ) + 1;
        return String.format("INV-%s-%04d", datePart, count);
    }

    private SaleItemResponse toSaleItemResponse(SaleItem item) {
        return SaleItemResponse.builder()
                .id(item.getId())
                .productName(item.getProductName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .lineTotal(item.getLineTotal())
                .build();
    }

    private SaleResponse toResponse(Sale sale) {
        return SaleResponse.builder()
                .id(sale.getId())
                .receiptNo(sale.getReceiptNo())
                .customerName(sale.getCustomerName())
                .customerPhone(sale.getCustomerPhone())
                .customerVillage(sale.getCustomerVillage())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getId() : null)
                .soldBy(sale.getSoldBy().getName())
                .paymentMethod(sale.getPaymentMethod())
                .items(sale.getSaleItems().stream()
                        .map(this::toSaleItemResponse)
                        .collect(Collectors.toList()))
                .subtotal(sale.getSubtotal())
                .discountValue(sale.getDiscountValue())
                .discountType(sale.getDiscountType())
                .grandTotal(sale.getGrandTotal())
                .isCredit(sale.getCreditSale())
                .createdAt(sale.getCreatedAt())
                .build();
    }

    @Override
    public SaleResponse getById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + id));
        return toResponse(sale);
    }
}