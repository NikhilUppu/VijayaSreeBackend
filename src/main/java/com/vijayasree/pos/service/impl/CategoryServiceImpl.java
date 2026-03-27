package com.vijayasree.pos.service.impl;

import com.vijayasree.pos.dto.request.CategoryRequest;
import com.vijayasree.pos.dto.response.CategoryResponse;
import com.vijayasree.pos.entity.Category;
import com.vijayasree.pos.exceptions.ResourceNotFoundException;
import com.vijayasree.pos.repository.CategoryRepository;
import com.vijayasree.pos.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Category already exists: " + request.getName());
        }
        Category category = Category.builder()
                .name(request.getName())
                .build();
        return toResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findById(id);
        category.setName(request.getName());
        return toResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        categoryRepository.delete(findById(id));
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .build();
    }
}