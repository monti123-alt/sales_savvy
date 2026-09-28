package com.salessavvy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A sales order placed by one customer, containing many order items.
 * Table: orders
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-friendly reference like "ORD-2026-0001".
     * Unique so support can find an order by this number.
     */
    @Column(nullable = false, unique = true, length = 30)
    private String orderNumber;

    /**
     * MANY orders belong to ONE customer.
     *
     * @ManyToOne        = many-to-one relationship
     * @JoinColumn(name = "customer_id", nullable = false) = the FOREIGN KEY column
     * fetch = FetchType.EAGER               = load the customer with the order
     *                                          (we always need the customer name)
     * @JsonIgnore                             = do not serialize the whole customer
     *                                          object (avoids infinite recursion)
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.CREATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30,
            columnDefinition = "varchar(30) not null default 'CASH_ON_DELIVERY'")
    private PaymentMethod paymentMethod = PaymentMethod.CASH_ON_DELIVERY;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20,
            columnDefinition = "varchar(20) not null default 'PENDING'")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "gateway_order_id", length = 80)
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id", length = 80)
    private String gatewayPaymentId;

    /**
     * ONE order has MANY order items.
     *
     * cascade = CascadeType.ALL  -> saving/deleting the order also saves/deletes items
     * orphanRemoval = true      -> removing an item from the list deletes the row
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Total money of the order. We store it (not only calculate on the fly)
     * because historical totals must not change if a product price changes later.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    @Column(length = 500)
    private String notes;

    public Order() {
    }

    public Order(String orderNumber, Customer customer) {
        this.orderNumber = orderNumber;
        this.customer = customer;
    }

    // ---- relationship helpers ----

    /** Add an item and set its back-reference to this order. */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    // ---- getters and setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getGatewayOrderId() { return gatewayOrderId; }
    public void setGatewayOrderId(String gatewayOrderId) { this.gatewayOrderId = gatewayOrderId; }

    public String getGatewayPaymentId() { return gatewayPaymentId; }
    public void setGatewayPaymentId(String gatewayPaymentId) { this.gatewayPaymentId = gatewayPaymentId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "Order{id=" + id + ", orderNumber='" + orderNumber + "', status=" + status
                + ", totalAmount=" + totalAmount + "}";
    }
}
