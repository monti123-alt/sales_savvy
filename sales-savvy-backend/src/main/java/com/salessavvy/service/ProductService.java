package com.salessavvy.service;

import com.salessavvy.dto.ProductRequest;
import com.salessavvy.dto.ProductResponse;
import com.salessavvy.entity.Product;
import com.salessavvy.exception.CustomException;
import com.salessavvy.exception.ResourceNotFoundException;
import com.salessavvy.repository.OrderItemRepository;
import com.salessavvy.repository.ProductRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for products.
 * The controller will only call methods on this class - it never touches the repository.
 */
@Service
@Transactional(readOnly = true)   // every method is read-only unless it says otherwise
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductImageStorageService imageStorageService;

    /**
     * Constructor injection: Spring passes the repositories in automatically.
     * We store them in final fields (final = cannot be changed after construction).
     */
    public ProductService(ProductRepository productRepository,
                          OrderItemRepository orderItemRepository,
                          ProductImageStorageService imageStorageService) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.imageStorageService = imageStorageService;
    }

    // ---------- CREATE ----------
    // @Transactional (read-write) = wrap in a DB transaction
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        return createProduct(request, null);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request, MultipartFile image) {
        // Business rule: SKU must be unique
        if (productRepository.existsBySku(request.getSku())) {
            throw new CustomException("Product with SKU '" + request.getSku() + "' already exists");
        }

        validateProductUrls(request);
        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());
        product.setImageUrl(image != null && !image.isEmpty()
                ? imageStorageService.store(image)
                : emptyToNull(request.getImageUrl()));
        product.setProductUrl(emptyToNull(request.getProductUrl()));
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }

    // ---------- READ ----------
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
        return ProductResponse.from(product);
    }

    /** The entity itself - needed by OrderService to read the real price. */
    public Product getProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
    }

    // ---------- UPDATE ----------
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        return updateProduct(id, request, null);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, MultipartFile image) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));

        // If the SKU changed, make sure the new one is not taken
        if (!product.getSku().equalsIgnoreCase(request.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new CustomException("Product with SKU '" + request.getSku() + "' already exists");
        }

        validateProductUrls(request);
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());
        if (image != null && !image.isEmpty()) {
            product.setImageUrl(imageStorageService.store(image));
        } else if (request.getImageUrl() != null) {
            product.setImageUrl(emptyToNull(request.getImageUrl()));
        }
        product.setProductUrl(emptyToNull(request.getProductUrl()));
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }

    private void validateProductUrls(ProductRequest request) {
        validateHttpUrl(request.getProductUrl(), "Product link");
        String imageUrl = request.getImageUrl();
        if (imageUrl != null && imageUrl.startsWith("/api/product-images/")
                && !imageUrl.matches("/api/product-images/[a-f0-9-]{36}\\.(png|jpg|gif|webp)")) {
            throw new CustomException("Image URL is invalid");
        }
        if (imageUrl != null && !imageUrl.isBlank()
                && !imageUrl.startsWith("/api/product-images/")) {
            validateHttpUrl(imageUrl, "Image URL");
        }
    }

    private void validateHttpUrl(String value, String fieldName) {
        if (value == null || value.isBlank()) return;
        try {
            URI uri = URI.create(value.trim());
            if (!("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ex) {
            throw new CustomException(fieldName + " must be a valid HTTP or HTTPS URL");
        }
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    // ---------- DELETE ----------
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));

        // Business rule: cannot delete a product that appears in existing orders,
        // otherwise old invoices would lose their product reference.
        long usedIn = orderItemRepository.countByProductId(id);
        if (usedIn > 0) {
            throw new CustomException(
                    "Product is used in " + usedIn + " order item(s) and cannot be deleted. " +
                    "Mark it as inactive instead.");
        }

        productRepository.delete(product);
    }

    // ---------- SEARCH & FILTER ----------
    public List<ProductResponse> searchProducts(String keyword, String category) {
        // Empty string -> null, because JPQL `LIKE '%%'` matches everything anyway,
        // but null makes the intent explicit.
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String cat = (category == null || category.isBlank()) ? null : category.trim();

        return productRepository.searchProducts(kw, cat).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }
}
