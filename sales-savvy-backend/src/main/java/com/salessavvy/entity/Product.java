package com.salessavvy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A product we sell. Table: products
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_category", columnList = "category")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Name must be at most 150 characters")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "SKU is required")
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 80)
    private String category;

    @Column(length = 1000)
    private String description;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "product_url", length = 2048)
    private String productUrl;

    /**
     * BigDecimal is used for money (never double/float - they have rounding errors).
     * precision=10, scale=2  =>  max 99,999,999.99
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have max 2 decimal places")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    /**
     * 0 = available, 1 = discontinued.
     */
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Product() {
    }

    public Product(String name, String sku, String category, BigDecimal price, Integer stockQuantity) {
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // Called by JPA right before an UPDATE, so updated_at is always current
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---- getters and setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getProductUrl() { return productUrl; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', sku='" + sku + "', price=" + price + "}";
    }
}
