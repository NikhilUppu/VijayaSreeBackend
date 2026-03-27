package com.vijayasree.pos.repository;

import com.vijayasree.pos.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCase(String sku);

    List<Product> findByActiveTrue();

    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProducts(String query);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockQty <= p.lowStockThreshold")
    List<Product> findLowStockProducts();

    @Query("SELECT DISTINCT p.company FROM Product p WHERE p.company IS NOT NULL AND p.active = true ORDER BY p.company")
    List<String> findAllCompanies();

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
            "LOWER(p.company) = LOWER(:company)")
    List<Product> findByCompany(String company);


    @Query("SELECT p FROM Product p WHERE p.active = true " +
            "AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:company IS NULL OR LOWER(p.company) = LOWER(:company)) " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Product> findAllFiltered(
            @Param("search") String search,
            @Param("company") String company,
            @Param("categoryId") Long categoryId,
            Pageable pageable);
}