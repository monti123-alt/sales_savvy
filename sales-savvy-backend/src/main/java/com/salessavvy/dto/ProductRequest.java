package com.salessavvy.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * What the client SENDS to create/update a product.
 *
 * No `id` field on purpose: if a client could set the id it could
 * overwrite a different product. The server always decides the id.
 */
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 150, message = "Name must be at most 150 characters")
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must be at most 50 characters")
    private String sku;

    @NotBlank(message = "Category is required")
    @Size(max = 80, message = "Category must be at most 80 characters")
    private String category;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @Size(max = 2048, message = "Image URL must be at most 2048 characters")
    private String imageUrl;

    @Size(max = 2048, message = "Product URL must be at most 2048 characters")
    private String productUrl;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQuantity;

    // ---- getters and setters ----
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
}
