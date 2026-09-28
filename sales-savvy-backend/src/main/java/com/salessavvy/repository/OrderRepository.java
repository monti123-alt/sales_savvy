package com.salessavvy.repository;

import com.salessavvy.entity.Order;
import com.salessavvy.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    // All orders of one customer, newest first
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId ORDER BY o.orderDate DESC")
    List<Order> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT o FROM Order o WHERE LOWER(o.customer.email) = LOWER(:email) ORDER BY o.orderDate DESC")
    List<Order> findByCustomerEmail(@Param("email") String email);

    @Query("SELECT o FROM Order o WHERE o.id = :orderId AND LOWER(o.customer.email) = LOWER(:email)")
    Optional<Order> findByIdAndCustomerEmail(@Param("orderId") Long orderId,
                                             @Param("email") String email);

    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);

    List<Order> findAllByOrderByOrderDateDesc();

    // --- Dashboard: total revenue from all non-cancelled orders ---
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status <> com.salessavvy.entity.OrderStatus.CANCELLED")
    BigDecimal sumTotalSales();

    // Same, but only for orders in a date range (used by a "sales this month" card)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.status <> com.salessavvy.entity.OrderStatus.CANCELLED " +
           "AND o.orderDate BETWEEN :from AND :to")
    BigDecimal sumTotalSalesBetween(@Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

    // Count orders placed inside a date range
    long countByOrderDateBetween(LocalDateTime from, LocalDateTime to);

    // Every distinct order number, highest first -> used to generate the next order number
    @Query("SELECT o.orderNumber FROM Order o ORDER BY o.id DESC")
    List<String> findRecentOrderNumbers();

    // Revenue grouped by status (for the dashboard chart)
    @Query("SELECT o.status, COALESCE(SUM(o.totalAmount), 0) FROM Order o GROUP BY o.status")
    List<Object[]> sumTotalAmountGroupedByStatus();
}
