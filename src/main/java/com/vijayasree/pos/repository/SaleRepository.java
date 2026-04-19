package com.vijayasree.pos.repository;

import com.vijayasree.pos.entity.Sale;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(s.grandTotal), 0) FROM Sale s " +
            "WHERE s.createdAt BETWEEN :start AND :end")
    java.math.BigDecimal sumRevenueByDateRange(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Sale> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COALESCE(SUM(s.grandTotal), 0) FROM Sale s WHERE s.customer.id = :customerId")
    java.math.BigDecimal sumGrandTotalByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT MAX(s.createdAt) FROM Sale s WHERE s.customer.id = :customerId")
    Optional<LocalDateTime> findLastVisitByCustomerId(@Param("customerId") Long customerId);

    @Modifying
    @Transactional
    @Query("UPDATE Sale s SET s.soldBy = null WHERE s.soldBy.id = :userId")
    void clearSoldBy(@Param("userId") Long userId);

    // Used by Print Station on reconnect to catch up on missed bills
    List<Sale> findByPrintedFalseOrderByCreatedAtAsc();
}