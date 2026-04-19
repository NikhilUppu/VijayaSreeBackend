
package com.vijayasree.pos.entity;
import com.vijayasree.pos.entity.SaleItem;
import com.vijayasree.pos.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sold_by", nullable = true)
    private User soldBy;

    @Column(nullable = false, unique = true)
    private String receiptNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column
    private String customerName;

    @Column
    private String customerPhone;

    @Column
    private String customerVillage;

    @Column(nullable = false)
    @Builder.Default
    private Boolean creditSale = false;

    @Enumerated(EnumType.STRING)
    @Column
    private PaymentMethod paymentMethod;

    public enum PaymentMethod {
        CASH, UPI, CARD
    }


    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal grandTotal;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SaleItem> saleItems;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // False until the cashier's Print Station confirms it was sent to the printer
    @Builder.Default
    @Column(nullable = false)
    private Boolean printed = false;

    public enum DiscountType {
        FLAT, PERCENTAGE
    }
}