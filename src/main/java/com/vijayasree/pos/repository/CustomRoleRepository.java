package com.vijayasree.pos.repository;

import com.vijayasree.pos.entity.CustomRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CustomRoleRepository extends JpaRepository<CustomRole, Long> {

    Optional<CustomRole> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<CustomRole> findByIsSystemTrue();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.id = :roleId")
    long countUsersWithRole(Long roleId);
}