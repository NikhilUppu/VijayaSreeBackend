package com.vijayasree.pos.service.impl;

import com.vijayasree.pos.dto.request.ProductRequest;
import com.vijayasree.pos.dto.request.StockAdjustmentRequest;
import com.vijayasree.pos.dto.response.ProductResponse;
import com.vijayasree.pos.entity.Category;
import com.vijayasree.pos.entity.Product;
import com.vijayasree.pos.entity.StockAdjustment;
import com.vijayasree.pos.entity.User;
import com.vijayasree.pos.exceptions.ResourceNotFoundException;
import com.vijayasree.pos.repository.CategoryRepository;
import com.vijayasree.pos.repository.ProductRepository;
import com.vijayasree.pos.repository.StockAdjustmentRepository;
import com.vijayasree.pos.repository.UserRepository;
import com.vijayasree.pos.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;




@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final UserRepository userRepository;

    @Override
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.getSku())) {
            throw new IllegalArgumentException("SKU already exists: " + request.getSku());
        }
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .company(request.getCompany())
                .sku(request.getSku().toUpperCase())
                .category(category)
                .price(request.getPrice())
                .stockQty(request.getStockQty())
                .lowStockThreshold(request.getLowStockThreshold())
                .notes(request.getNotes())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Override
    public List<ProductResponse> getAll() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> search(String query) {
        return productRepository.searchProducts(query)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getLowStock() {
        return productRepository.findLowStockProducts()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findById(id);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setCompany(request.getCompany());
        product.setSku(request.getSku().toUpperCase());
        product.setCategory(category);
        product.setPrice(request.getPrice());
        product.setStockQty(request.getStockQty());
        product.setLowStockThreshold(request.getLowStockThreshold());
        product.setNotes(request.getNotes());
        product.setImageUrl(request.getImageUrl());

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse adjustStock(Long id, StockAdjustmentRequest request) {
        Product product = findById(id);
        User adjustedBy = userRepository.findById(request.getAdjustedById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getAdjustedById()));

        StockAdjustment adjustment = StockAdjustment.builder()
                .product(product)
                .adjustedBy(adjustedBy)
                .oldQty(product.getStockQty())
                .newQty(request.getNewQty())
                .reason(request.getReason())
                .build();

        stockAdjustmentRepository.save(adjustment);
        product.setStockQty(request.getNewQty());
        return toResponse(productRepository.save(product));
    }

    @Override
    public void delete(Long id) {
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private String resolveStockStatus(Product product) {
        if (product.getStockQty() == 0) return "OUT_OF_STOCK";
        if (product.getStockQty() <= product.getLowStockThreshold()) return "LOW_STOCK";
        return "IN_STOCK";
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .company(product.getCompany())
                .sku(product.getSku())
                .categoryName(product.getCategory().getName())
                .price(product.getPrice())
                .stockQty(product.getStockQty())
                .lowStockThreshold(product.getLowStockThreshold())
                .notes(product.getNotes())
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .stockStatus(resolveStockStatus(product))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Override
    public List<ProductResponse> getByCompany(String company) {
        return productRepository.findByCompany(company)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCompanies() {
        return productRepository.findAllCompanies();
    }

    @Override
    public Map<String, Object> getAllPaginated(int page, int size, String search,
                                               String company, Long categoryId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<Product> productPage = productRepository.findAllFiltered(
                search != null && !search.isBlank() ? search : null,
                company != null && !company.isBlank() ? company : null,
                categoryId,
                pageable
        );

        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent()
                .stream().map(this::toResponse).collect(Collectors.toList()));
        response.put("currentPage", productPage.getNumber());
        response.put("totalPages", productPage.getTotalPages());
        response.put("totalItems", productPage.getTotalElements());
        response.put("pageSize", size);
        return response;
    }
}