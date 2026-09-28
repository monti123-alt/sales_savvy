package com.salessavvy.repository;

import com.salessavvy.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // --- Derived queries (Spring builds the SQL from the method name) ---

    // SQL: WHERE sku = ?
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    // SQL: WHERE category = ?
    List<Product> findByCategory(String category);

    // SQL: WHERE category = ? AND active = true
    List<Product> findByCategoryAndActiveTrue(String category);

    // SQL: WHERE active = true ORDER BY name ASC
    List<Product> findByActiveTrueOrderByNameAsc();

    // --- Search: name OR sku OR description contains the keyword (case-insensitive) ---
    // The % wildcards are SQL LIKE wildcards. This is the "search product" API.
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    // --- Search + category combined (used by the Products page filters) ---
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "                          OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.name ASC")
    List<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("category") String category);

    // --- Get every distinct category, for building the filter dropdown ---
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category ASC")
    List<String> findDistinctCategories();

    // --- Dashboard: sum of all active product stock value ---
    @Query("SELECT COALESCE(SUM(p.stockQuantity * p.price), 0) FROM Product p WHERE p.active = true")
    BigDecimal sumInventoryValue();

    // --- Low stock items: stock at or below the given level ---
    List<Product> findByStockQuantityLessThanEqualAndActiveTrueOrderByStockQuantityAsc(int level);
}
