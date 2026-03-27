package com.vijayasree.pos.repository;

import com.vijayasree.pos.entity.Permission;
import com.vijayasree.pos.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    List<UserPermission> findByUserId(Long userId);

    Optional<UserPermission> findByUserIdAndPermission(Long userId, Permission permission);

    @Query("SELECT up FROM UserPermission up WHERE up.user.id = :userId AND up.grantType = 'GRANT'")
    List<UserPermission> findGrantedByUserId(Long userId);

    @Query("SELECT up FROM UserPermission up WHERE up.user.id = :userId AND up.grantType = 'REVOKE'")
    List<UserPermission> findRevokedByUserId(Long userId);

    void deleteByUserId(Long userId);
}