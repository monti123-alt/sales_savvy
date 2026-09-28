package com.salessavvy.repository;

import com.salessavvy.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // All line items of one order
    List<OrderItem> findByOrderId(Long orderId);

    // Which order items reference this product (needed before deleting a product)
    long countByProductId(Long productId);

    // Best selling products: group by product, sum quantity, order by best seller first
    @Query("SELECT i.product.id, i.productName, SUM(i.quantity), SUM(i.lineTotal) " +
           "FROM OrderItem i GROUP BY i.product.id, i.productName " +
           "ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findTopSellingProducts();
}
