package com.salessavvy.dto;

import com.salessavvy.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * What we SEND BACK to the client for a product.
 * Read-only: only getters, no setters.
 */
public class ProductResponse {

    private Long id;
    private String name;
    private String sku;
    private String category;
    private String description;
    private String imageUrl;
    private String productUrl;
    private BigDecimal price;
    private Integer stockQuantity;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductResponse() {
    }

    /** Build a response from an entity. This is the entity -> DTO conversion. */
    public static ProductResponse from(Product product) {
        if (product == null) {
            return null;
        }
        ProductResponse dto = new ProductResponse();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.sku = product.getSku();
        dto.category = product.getCategory();
        dto.description = product.getDescription();
        dto.imageUrl = product.getImageUrl();
        dto.productUrl = product.getProductUrl();
        dto.price = product.getPrice();
        dto.stockQuantity = product.getStockQuantity();
        dto.active = product.isActive();
        dto.createdAt = product.getCreatedAt();
        dto.updatedAt = product.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getProductUrl() { return productUrl; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
