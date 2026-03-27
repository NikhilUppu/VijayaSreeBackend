package com.vijayasree.pos.repository;

import com.vijayasree.pos.entity.StockAdjustment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {
    List<StockAdjustment> findByProductIdOrderByCreatedAtDesc(Long productId);
    @Modifying
    @Transactional
    @Query("UPDATE StockAdjustment s SET s.adjustedBy = null WHERE s.adjustedBy.id = :userId")
    void clearAdjustedBy(@Param("userId") Long userId);
}