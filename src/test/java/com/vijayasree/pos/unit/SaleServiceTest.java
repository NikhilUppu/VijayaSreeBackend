package com.vijayasree.pos.unit;

import com.vijayasree.pos.dto.request.CheckoutItemRequest;
import com.vijayasree.pos.dto.request.CheckoutRequest;
import com.vijayasree.pos.dto.response.SaleResponse;
import com.vijayasree.pos.entity.*;
import com.vijayasree.pos.exceptions.InsufficientStockException;
import com.vijayasree.pos.exceptions.ResourceNotFoundException;
import com.vijayasree.pos.repository.*;
import com.vijayasree.pos.service.impl.SaleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock SaleRepository saleRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock CustomerRepository customerRepository;

    @InjectMocks SaleServiceImpl saleService;

    private User mockUser;
    private Product mockProduct;
    private Category mockCategory;

    @BeforeEach
    void setup() {
        mockCategory = Category.builder().id(1L).name("Pesticides").build();

        mockProduct = Product.builder()
                .id(1L)
                .name("Mancozeb 250g")
                .sku("FUNG-001")
                .price(new BigDecimal("200.00"))
                .stockQty(20)
                .lowStockThreshold(5)
                .category(mockCategory)
                .active(true)
                .build();

        mockUser = User.builder()
                .id(1L)
                .name("Ramamohan")
                .username("ramamohan")
                .active(true)
                .build();
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private CheckoutRequest buildRequest(String discountType,
                                         BigDecimal discountValue) {
        CheckoutItemRequest item = new CheckoutItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);

        CheckoutRequest req = new CheckoutRequest();
        req.setSoldById(1L);
        req.setItems(List.of(item));
        req.setDiscountType(discountType != null
                ? Sale.DiscountType.valueOf(discountType) : null);
        req.setDiscountValue(discountValue);
        req.setPaymentMethod(Sale.PaymentMethod.CASH);
        req.setIsCredit(false);
        return req;
    }

    private Sale buildSale(BigDecimal grandTotal) {
        SaleItem saleItem = SaleItem.builder()
                .id(1L)
                .productName("Mancozeb 250g")
                .unitPrice(new BigDecimal("200.00"))
                .quantity(2)
                .lineTotal(new BigDecimal("400.00"))
                .build();

        Sale sale = Sale.builder()
                .id(1L)
                .receiptNo("INV-20260325-0001")
                .soldBy(mockUser)
                .customerName("Walk-in Customer")
                .subtotal(new BigDecimal("400.00"))
                .grandTotal(grandTotal)
                .creditSale(false)
                .paymentMethod(Sale.PaymentMethod.CASH)
                .saleItems(List.of(saleItem))
                .build();

        saleItem.setSale(sale);
        return sale;
    }

    // ─── tests ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Checkout with no discount — grandTotal equals subtotal")
    void checkout_noDiscount_grandTotalEqualsSubtotal() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any())).thenReturn(mockProduct);
        when(saleRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(saleRepository.save(any())).thenReturn(buildSale(new BigDecimal("400.00")));

        SaleResponse response = saleService.checkout(buildRequest(null, null));

        // 2 × ₹200 = ₹400, no discount
        assertThat(response.getGrandTotal())
                .isEqualByComparingTo(new BigDecimal("400.00"));
    }

    @Test
    @DisplayName("Checkout with FLAT discount — grandTotal is subtotal minus flat amount")
    void checkout_flatDiscount_subtractsFromSubtotal() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any())).thenReturn(mockProduct);
        when(saleRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(saleRepository.save(any())).thenReturn(buildSale(new BigDecimal("350.00")));

        // ₹400 subtotal - ₹50 flat = ₹350
        SaleResponse response = saleService.checkout(
                buildRequest("FLAT", new BigDecimal("50")));

        assertThat(response.getGrandTotal())
                .isEqualByComparingTo(new BigDecimal("350.00"));
    }

    @Test
    @DisplayName("Checkout with PERCENTAGE discount — grandTotal is subtotal minus percent amount")
    void checkout_percentageDiscount_appliesCorrectly() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any())).thenReturn(mockProduct);
        when(saleRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        // ₹400 - 10% (₹40) = ₹360
        when(saleRepository.save(any())).thenReturn(buildSale(new BigDecimal("360.00")));

        SaleResponse response = saleService.checkout(
                buildRequest("PERCENTAGE", new BigDecimal("10")));

        assertThat(response.getGrandTotal())
                .isEqualByComparingTo(new BigDecimal("360.00"));
    }

    @Test
    @DisplayName("Checkout with insufficient stock — throws InsufficientStockException")
    void checkout_insufficientStock_throwsException() {
        mockProduct.setStockQty(1); // only 1 in stock

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        CheckoutRequest req = buildRequest(null, null);
        req.getItems().get(0).setQuantity(5); // trying to buy 5

        assertThatThrownBy(() -> saleService.checkout(req))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Checkout with unknown user — throws ResourceNotFoundException")
    void checkout_userNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.checkout(buildRequest(null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Checkout with unknown product — throws ResourceNotFoundException")
    void checkout_productNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.checkout(buildRequest(null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("Credit sale exceeding limit — throws IllegalArgumentException")
    void checkout_creditExceedsLimit_throwsException() {
        Customer customer = Customer.builder()
                .id(1L)
                .name("Farmer A")
                .phone("9999999999")
                .creditLimit(new BigDecimal("300.00"))
                .creditBalance(new BigDecimal("200.00")) // ₹100 available
                .active(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any())).thenReturn(mockProduct);
        when(customerRepository.findByPhone("9999999999"))
                .thenReturn(Optional.of(customer));

        CheckoutRequest req = buildRequest(null, null);
        req.setIsCredit(true);
        req.setCustomerPhone("9999999999");
        // grandTotal = ₹400, but only ₹100 credit available

        assertThatThrownBy(() -> saleService.checkout(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credit limit exceeded");
    }

    @Test
    @DisplayName("Checkout decrements product stock quantity")
    void checkout_success_decrementsStock() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(saleRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(saleRepository.save(any())).thenReturn(buildSale(new BigDecimal("400.00")));

        saleService.checkout(buildRequest(null, null));

        // was 20, bought 2 → should be 18
        assertThat(mockProduct.getStockQty()).isEqualTo(18);
    }
}