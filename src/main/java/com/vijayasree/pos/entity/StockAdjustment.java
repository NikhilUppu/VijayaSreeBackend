package com.vijayasree.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_adjustments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjusted_by", nullable = true)
    private User adjustedBy;

    @Column(nullable = false)
    private Integer oldQty;

    @Column(nullable = false)
    private Integer newQty;

    private String reason;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}