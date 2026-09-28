package com.salessavvy.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * The dashboard payload: the 4 numbers at the top plus a few extras.
 */
public class DashboardStats {

    private long totalProducts;
    private long totalCustomers;
    private long totalOrders;
    private BigDecimal totalSales;
    private BigDecimal totalSalesThisMonth;
    private long ordersThisMonth;
    private long lowStockProducts;
    private BigDecimal inventoryValue;
    private List<Map<String, Object>> salesByStatus;
    private List<Map<String, Object>> topProducts;

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }

    public BigDecimal getTotalSalesThisMonth() { return totalSalesThisMonth; }
    public void setTotalSalesThisMonth(BigDecimal v) { this.totalSalesThisMonth = v; }

    public long getOrdersThisMonth() { return ordersThisMonth; }
    public void setOrdersThisMonth(long ordersThisMonth) { this.ordersThisMonth = ordersThisMonth; }

    public long getLowStockProducts() { return lowStockProducts; }
    public void setLowStockProducts(long v) { this.lowStockProducts = v; }

    public BigDecimal getInventoryValue() { return inventoryValue; }
    public void setInventoryValue(BigDecimal inventoryValue) { this.inventoryValue = inventoryValue; }

    public List<Map<String, Object>> getSalesByStatus() { return salesByStatus; }
    public void setSalesByStatus(List<Map<String, Object>> salesByStatus) { this.salesByStatus = salesByStatus; }

    public List<Map<String, Object>> getTopProducts() { return topProducts; }
    public void setTopProducts(List<Map<String, Object>> topProducts) { this.topProducts = topProducts; }
}
