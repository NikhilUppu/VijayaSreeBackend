package com.vijayasree.pos.repository;

import com.vijayasree.pos.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByPhone(String phone);

    boolean existsByPhone(String phone);

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "c.phone LIKE CONCAT('%', :query, '%')")
    List<Customer> searchCustomers(String query);

    @Query("SELECT c FROM Customer c WHERE c.creditBalance > 0 ORDER BY c.creditBalance DESC")
    List<Customer> findCreditCustomers();
}