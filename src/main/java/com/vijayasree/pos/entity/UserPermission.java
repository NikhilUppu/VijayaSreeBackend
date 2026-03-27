package com.vijayasree.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "permission"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Permission permission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrantType grantType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", nullable = false)
    private User grantedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum GrantType {
        GRANT,
        REVOKE
    }
}