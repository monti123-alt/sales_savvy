package com.salessavvy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import com.salessavvy.entity.PaymentMethod;

import java.util.List;

/**
 * What the client sends to create an order.
 */
public class OrderRequest {

    private Long customerId;

    @NotEmpty(message = "Order must contain at least one item")
    @Size(max = 100, message = "An order can have at most 100 items")
    @Valid   // <-- IMPORTANT: makes validation cascade into each OrderItemRequest
    private List<OrderItemRequest> items;

    @Size(max = 500, message = "Shipping address must be at most 500 characters")
    private String shippingAddress;

    @Size(max = 500, message = "Notes must be at most 500 characters")
    private String notes;

    private PaymentMethod paymentMethod = PaymentMethod.CASH_ON_DELIVERY;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
