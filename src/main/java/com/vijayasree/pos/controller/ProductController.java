package com.vijayasree.pos.controller;

import com.vijayasree.pos.dto.request.ProductRequest;
import com.vijayasree.pos.dto.request.StockAdjustmentRequest;
import com.vijayasree.pos.dto.response.ProductResponse;
import com.vijayasree.pos.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.vijayasree.pos.dto.response.ApiResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Long categoryId) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.getAllPaginated(page, size, search, company, categoryId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getByCategory(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(productService.search(query));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStock() {
        return ResponseEntity.ok(productService.getLowStock());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> adjustStock(@PathVariable Long id,
                                                       @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(productService.adjustStock(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/companies")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ResponseEntity<ApiResponse<List<String>>> getCompanies() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllCompanies()));
    }

    @GetMapping("/company/{company}")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCompany(
            @PathVariable String company) {
        return ResponseEntity.ok(ApiResponse.success(productService.getByCompany(company)));
    }
}