package com.salessavvy.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * One line inside an order: which product, how many, at what price.
 * Table: order_items
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The parent order. "order" here is the foreign key column.
     * mappedBy = "order" on the other side.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    /**
     * The product being bought.
     * EAGER because we always render product name/price in the order details screen.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * We copy name/price at the time of purchase.
     * If the product is later renamed or repriced, the old invoice must not change.
     */
    @Column(nullable = false, length = 150)
    private String productName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * lineTotal = unitPrice * quantity.
     * We store it so the total is always reproducible.
     */
    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    public OrderItem() {
    }

    public OrderItem(Product product, Integer quantity) {
        this.product = product;
        this.productName = product.getName();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
        this.lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    @Override
    public String toString() {
        return "OrderItem{product='" + productName + "', qty=" + quantity
                + ", lineTotal=" + lineTotal + "}";
    }
}
