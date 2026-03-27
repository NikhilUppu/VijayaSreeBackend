package com.vijayasree.pos.unit;

import com.vijayasree.pos.dto.request.ProductRequest;
import com.vijayasree.pos.dto.request.StockAdjustmentRequest;
import com.vijayasree.pos.dto.response.ProductResponse;
import com.vijayasree.pos.entity.Category;
import com.vijayasree.pos.entity.Product;
import com.vijayasree.pos.entity.User;
import com.vijayasree.pos.exceptions.ResourceNotFoundException;
import com.vijayasree.pos.repository.*;
import com.vijayasree.pos.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock StockAdjustmentRepository stockAdjustmentRepository;
    @Mock UserRepository userRepository;

    @InjectMocks ProductServiceImpl productService;

    private Category mockCategory;
    private Product mockProduct;
    private User mockUser;

    @BeforeEach
    void setup() {
        mockCategory = Category.builder()
                .id(1L).name("Pesticides").build();

        mockProduct = Product.builder()
                .id(1L)
                .name("Chlorpyrifos 500ml")
                .sku("PEST-001")
                .price(new BigDecimal("350.00"))
                .stockQty(15)
                .lowStockThreshold(5)
                .category(mockCategory)
                .active(true)
                .build();

        mockUser = User.builder()
                .id(1L).name("Ramamohan").active(true).build();
    }

    @Test
    @DisplayName("Create product — SKU is saved in uppercase")
    void create_skuSavedAsUppercase() {
        ProductRequest req = new ProductRequest();
        req.setName("Test Product");
        req.setSku("pest-lowercase");
        req.setCategoryId(1L);
        req.setPrice(new BigDecimal("100.00"));
        req.setStockQty(10);
        req.setLowStockThreshold(2);

        when(productRepository.existsBySkuIgnoreCase("pest-lowercase"))
                .thenReturn(false);
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(mockCategory));
        when(productRepository.save(any())).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });

        ProductResponse response = productService.create(req);

        assertThat(response.getSku()).isEqualTo("PEST-LOWERCASE");
    }

    @Test
    @DisplayName("Create product with duplicate SKU — throws IllegalArgumentException")
    void create_duplicateSku_throwsException() {
        ProductRequest req = new ProductRequest();
        req.setSku("PEST-001");
        req.setCategoryId(1L);

        when(productRepository.existsBySkuIgnoreCase("PEST-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SKU already exists");
    }

    @Test
    @DisplayName("Create product with unknown category — throws ResourceNotFoundException")
    void create_unknownCategory_throwsException() {
        ProductRequest req = new ProductRequest();
        req.setSku("NEW-001");
        req.setCategoryId(99L);

        when(productRepository.existsBySkuIgnoreCase("NEW-001")).thenReturn(false);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    @DisplayName("Delete product — sets active to false (soft delete)")
    void delete_setsActiveToFalse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        productService.delete(1L);

        assertThat(mockProduct.getActive()).isFalse();
        verify(productRepository).save(mockProduct);
    }

    @Test
    @DisplayName("Stock status — OUT_OF_STOCK when stockQty is 0")
    void stockStatus_zeroQty_returnsOutOfStock() {
        mockProduct.setStockQty(0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        ProductResponse response = productService.getById(1L);

        assertThat(response.getStockStatus()).isEqualTo("OUT_OF_STOCK");
    }

    @Test
    @DisplayName("Stock status — LOW_STOCK when stockQty is at or below threshold")
    void stockStatus_atThreshold_returnsLowStock() {
        mockProduct.setStockQty(5);
        mockProduct.setLowStockThreshold(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        ProductResponse response = productService.getById(1L);

        assertThat(response.getStockStatus()).isEqualTo("LOW_STOCK");
    }

    @Test
    @DisplayName("Stock status — IN_STOCK when stockQty is above threshold")
    void stockStatus_aboveThreshold_returnsInStock() {
        mockProduct.setStockQty(20);
        mockProduct.setLowStockThreshold(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        ProductResponse response = productService.getById(1L);

        assertThat(response.getStockStatus()).isEqualTo("IN_STOCK");
    }

    @Test
    @DisplayName("Adjust stock — updates product stockQty to new value")
    void adjustStock_setsNewQuantity() {
        StockAdjustmentRequest req = new StockAdjustmentRequest();
        req.setAdjustedById(1L);
        req.setNewQty(50);
        req.setReason("Restock from supplier");

        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(stockAdjustmentRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.save(any())).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            return p;
        });

        ProductResponse response = productService.adjustStock(1L, req);

        assertThat(response.getStockQty()).isEqualTo(50);
        // verify adjustment history was saved
        verify(stockAdjustmentRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Get product by unknown ID — throws ResourceNotFoundException")
    void getById_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }
}