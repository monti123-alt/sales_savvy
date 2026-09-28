package com.salessavvy.dto;

import com.salessavvy.entity.OrderItem;

import java.math.BigDecimal;

/**
 * One line of an order as sent back to the client.
 */
public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;

    public OrderItemResponse() {
    }

    public static OrderItemResponse from(OrderItem item) {
        if (item == null) {
            return null;
        }
        OrderItemResponse dto = new OrderItemResponse();
        dto.id = item.getId();
        dto.productId = item.getProduct() != null ? item.getProduct().getId() : null;
        dto.productName = item.getProductName();
        dto.unitPrice = item.getUnitPrice();
        dto.quantity = item.getQuantity();
        dto.lineTotal = item.getLineTotal();
        return dto;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
