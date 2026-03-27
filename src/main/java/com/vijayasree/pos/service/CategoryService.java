package com.vijayasree.pos.service;

import com.vijayasree.pos.dto.request.CategoryRequest;
import com.vijayasree.pos.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    List<CategoryResponse> getAll();
    CategoryResponse getById(Long id);
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
}