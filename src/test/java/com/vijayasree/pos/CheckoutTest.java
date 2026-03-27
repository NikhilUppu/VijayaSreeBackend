package com.vijayasree.pos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijayasree.pos.entity.Product;
import com.vijayasree.pos.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CheckoutTest extends BaseIntegrationTest {

    @Autowired ObjectMapper objectMapper;
    @Autowired JwtUtil jwtUtil;

    private Product product;
    private String token;

    @BeforeEach
    void setup() {
        product = productRepository.save(Product.builder()
                .name("Chlorpyrifos 500ml")
                .sku("PEST-001")
                .company("Bayer")
                .category(category)
                .price(new BigDecimal("320.00"))
                .stockQty(50)
                .lowStockThreshold(10)
                .active(true)
                .build());

        token = jwtUtil.generateToken(
                adminUser.getUsername(),
                adminRole.getName(),
                "SALES_CHECKOUT,PRODUCT_VIEW"
        );
    }

    @Test
    @DisplayName("Checkout with 3 items decrements stock correctly")
    void checkoutDecrementsStock() throws Exception {
        mockMvc.perform(post("/api/sales/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "customerName", "Test Customer",
                                "customerPhone", "9876543210",
                                "paymentMethod", "CASH",
                                "isCredit", false,
                                "soldById", adminUser.getId(),
                                "items", List.of(Map.of(
                                        "productId", product.getId(),
                                        "quantity", 3
                                ))
                        ))))
                .andExpect(status().isCreated())  // ← 201
                .andExpect(jsonPath("$.grandTotal").value(960.00));

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getStockQty()).isEqualTo(47);
    }

    @Test
    @DisplayName("Checkout with flat discount calculates grand total correctly")
    void checkoutWithFlatDiscount() throws Exception {
        mockMvc.perform(post("/api/sales/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "customerName", "Walk-in Customer",
                                "paymentMethod", "UPI",
                                "isCredit", false,
                                "soldById", adminUser.getId(),
                                "discountValue", 20.00,
                                "discountType", "FLAT",
                                "items", List.of(Map.of(
                                        "productId", product.getId(),
                                        "quantity", 1
                                ))
                        ))))
                .andExpect(status().isCreated())  // ← 201
                .andExpect(jsonPath("$.grandTotal").value(300.00));
    }

    @Test
    @DisplayName("Checkout with percentage discount calculates correctly")
    void checkoutWithPercentageDiscount() throws Exception {
        mockMvc.perform(post("/api/sales/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "customerName", "Walk-in Customer",
                                "paymentMethod", "CASH",
                                "isCredit", false,
                                "soldById", adminUser.getId(),
                                "discountValue", 10.00,
                                "discountType", "PERCENTAGE",
                                "items", List.of(Map.of(
                                        "productId", product.getId(),
                                        "quantity", 1
                                ))
                        ))))
                .andExpect(status().isCreated())  // ← 201
                .andExpect(jsonPath("$.grandTotal").value(288.00));
    }

    @Test
    @DisplayName("Checkout with out of stock product returns error")
    void checkoutOutOfStock() throws Exception {
        product.setStockQty(0);
        productRepository.save(product);

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "customerName", "Walk-in",
                                "paymentMethod", "CASH",
                                "isCredit", false,
                                "soldById", adminUser.getId(),
                                "items", List.of(Map.of(
                                        "productId", product.getId(),
                                        "quantity", 1
                                ))
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Checkout with more quantity than stock returns error")
    void checkoutExceedsStock() throws Exception {
        mockMvc.perform(post("/api/sales/checkout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "customerName", "Walk-in",
                                "paymentMethod", "CASH",
                                "isCredit", false,
                                "soldById", adminUser.getId(),
                                "items", List.of(Map.of(
                                        "productId", product.getId(),
                                        "quantity", 999
                                ))
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Checkout without auth token returns 401 or 403")
    void checkoutNoAuth() throws Exception {
        mockMvc.perform(post("/api/sales/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "customerName", "Walk-in",
                                "paymentMethod", "CASH",
                                "isCredit", false,
                                "soldById", adminUser.getId(),
                                "items", List.of(Map.of(
                                        "productId", product.getId(),
                                        "quantity", 1
                                ))
                        ))))
                .andExpect(status().is4xxClientError()); // ← accepts 401 OR 403
    }
}