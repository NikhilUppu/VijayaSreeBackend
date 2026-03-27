package com.vijayasree.pos.service;

import com.vijayasree.pos.dto.request.ProductRequest;
import com.vijayasree.pos.dto.request.StockAdjustmentRequest;
import com.vijayasree.pos.dto.response.ProductResponse;

import java.util.List;
import java.util.Map;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    List<ProductResponse> getAll();
    List<ProductResponse> getByCategory(Long categoryId);
    List<ProductResponse> search(String query);
    List<ProductResponse> getLowStock();
    ProductResponse getById(Long id);
    ProductResponse update(Long id, ProductRequest request);
    ProductResponse adjustStock(Long id, StockAdjustmentRequest request);
    void delete(Long id);
    List<ProductResponse> getByCompany(String company);
    List<String> getAllCompanies();
    Map<String, Object> getAllPaginated(int page, int size, String search,
                                        String company, Long categoryId);
}