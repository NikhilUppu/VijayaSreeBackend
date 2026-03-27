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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProductTest extends BaseIntegrationTest {

    @Autowired ObjectMapper objectMapper;
    @Autowired JwtUtil jwtUtil;

    private String token;

    @BeforeEach
    void setup() {
        token = jwtUtil.generateToken(
                adminUser.getUsername(),
                adminRole.getName(),
                "PRODUCT_VIEW,PRODUCT_CREATE,PRODUCT_EDIT,PRODUCT_DELETE"
        );
    }

    @Test
    @DisplayName("Create product saves to DB correctly")
    void createProduct() throws Exception {
        long countBefore = productRepository.count();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Mancozeb 250g",
                                "sku", "FUNG-001",
                                "company", "Syngenta",
                                "categoryId", category.getId(),
                                "price", 180.00,
                                "stockQty", 30,
                                "lowStockThreshold", 10
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mancozeb 250g"))
                .andExpect(jsonPath("$.sku").value("FUNG-001"));

        // Check count increased by 1
        assertThat(productRepository.count()).isEqualTo(countBefore + 1);
    }

    @Test
    @DisplayName("Create product with duplicate SKU returns error")
    void createDuplicateSku() throws Exception {
        productRepository.save(Product.builder()
                .name("Existing Product")
                .sku("PEST-001")
                .category(category)
                .price(new BigDecimal("100.00"))
                .stockQty(10)
                .lowStockThreshold(5)
                .active(true)
                .build());

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Another Product",
                                "sku", "PEST-001",
                                "categoryId", category.getId(),
                                "price", 200.00,
                                "stockQty", 20,
                                "lowStockThreshold", 5
                        ))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Get products returns paginated list")
    void getProductsPaginated() throws Exception {
        // Create 3 products
        for (int i = 1; i <= 3; i++) {
            productRepository.save(Product.builder()
                    .name("Product " + i)
                    .sku("SKU-00" + i)
                    .category(category)
                    .price(new BigDecimal("100.00"))
                    .stockQty(10)
                    .lowStockThreshold(5)
                    .active(true)
                    .build());
        }

        mockMvc.perform(get("/api/products?page=0&size=50")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                // Response wraps in data object
                .andExpect(jsonPath("$.data.totalItems").value(3))
                .andExpect(jsonPath("$.data.products").isArray());
    }

    @Test
    @DisplayName("Delete product removes it from active products")
    void deleteProduct() throws Exception {
        Product p = productRepository.save(Product.builder()
                .name("To Delete")
                .sku("DEL-001")
                .category(category)
                .price(new BigDecimal("100.00"))
                .stockQty(10)
                .lowStockThreshold(5)
                .active(true)
                .build());

        Long deletedId = p.getId();

        mockMvc.perform(delete("/api/products/" + deletedId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is2xxSuccessful());

        // Product is soft deleted — active = false, still exists in DB
        Product deleted = productRepository.findById(deletedId).orElseThrow();
        assertThat(deleted.getActive()).isFalse();
    }
}