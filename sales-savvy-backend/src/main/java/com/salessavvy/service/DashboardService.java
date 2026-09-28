package com.salessavvy.service;

import com.salessavvy.dto.DashboardStats;
import com.salessavvy.repository.CustomerRepository;
import com.salessavvy.repository.OrderItemRepository;
import com.salessavvy.repository.OrderRepository;
import com.salessavvy.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    /** Anything with 5 or fewer units left is flagged as "low stock". */
    private static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public DashboardService(ProductRepository productRepository,
                            CustomerRepository customerRepository,
                            OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public DashboardStats getStats() {

        DashboardStats stats = new DashboardStats();

        // --- The four big numbers ---
        stats.setTotalProducts(productRepository.count());
        stats.setTotalCustomers(customerRepository.count());
        stats.setTotalOrders(orderRepository.count());
        stats.setTotalSales(orderRepository.sumTotalSales());

        // --- This month's numbers ---
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        stats.setTotalSalesThisMonth(orderRepository.sumTotalSalesBetween(startOfMonth, now));
        stats.setOrdersThisMonth(orderRepository.countByOrderDateBetween(startOfMonth, now));

        // --- Extra cards ---
        stats.setLowStockProducts(
                productRepository
                        .findByStockQuantityLessThanEqualAndActiveTrueOrderByStockQuantityAsc(LOW_STOCK_THRESHOLD)
                        .size());
        stats.setInventoryValue(productRepository.sumInventoryValue());

        // --- Revenue grouped by status ---
        List<Map<String, Object>> byStatus = new ArrayList<>();
        for (Object[] row : orderRepository.sumTotalAmountGroupedByStatus()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("status", row[0].toString());
            entry.put("amount", (BigDecimal) row[1]);
            byStatus.add(entry);
        }
        stats.setSalesByStatus(byStatus);

        // --- Top 5 selling products ---
        List<Map<String, Object>> top = new ArrayList<>();
        for (Object[] row : orderItemRepository.findTopSellingProducts()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("productId", row[0]);
            entry.put("productName", row[1]);
            entry.put("quantitySold", row[2]);
            entry.put("revenue", row[3]);
            top.add(entry);
        }
        stats.setTopProducts(top.stream().limit(5).toList());

        return stats;
    }
}
