package com.salessavvy.repository;

import com.salessavvy.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findFirstByEmailIgnoreCaseOrderByIdAsc(String email);

    List<Customer> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    // Search across name, email and phone in one call
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "c.phone LIKE CONCAT('%', :keyword, '%') " +
           "ORDER BY c.name ASC")
    List<Customer> searchCustomers(@Param("keyword") String keyword);
}
