package com.salessavvy.dto;

import com.salessavvy.entity.Order;
import com.salessavvy.entity.OrderItem;
import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.PaymentMethod;
import com.salessavvy.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * What we send back for an order.
 *
 * Note there is NO Customer object here, just customerId + customerName.
 * That completely avoids the infinite JSON loop.
 */
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private String shippingAddress;
    private String notes;

    public OrderResponse() {
    }

    public static OrderResponse from(Order order) {
        if (order == null) {
            return null;
        }
        OrderResponse dto = new OrderResponse();
        dto.id = order.getId();
        dto.orderNumber = order.getOrderNumber();
        if (order.getCustomer() != null) {
            dto.customerId = order.getCustomer().getId();
            dto.customerName = order.getCustomer().getName();
            dto.customerEmail = order.getCustomer().getEmail();
        }
        dto.status = order.getStatus();
        dto.paymentMethod = order.getPaymentMethod();
        dto.paymentStatus = order.getPaymentStatus();
        dto.items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());
        dto.totalAmount = order.getTotalAmount();
        dto.orderDate = order.getOrderDate();
        dto.shippingAddress = order.getShippingAddress();
        dto.notes = order.getNotes();
        return dto;
    }

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public OrderStatus getStatus() { return status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public List<OrderItemResponse> getItems() { return items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public String getShippingAddress() { return shippingAddress; }
    public String getNotes() { return notes; }
}
