package com.salessavvy.controller;

import com.salessavvy.dto.ApiResponse;
import com.salessavvy.dto.ProductRequest;
import com.salessavvy.dto.ProductResponse;
import com.salessavvy.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for products.
 *
 * @RestController = the return value of every method is serialized to JSON
 *                    (instead of being treated as a page/view name).
 * @RequestMapping("/api/products") = every method in this class starts with this URL.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ---------- CREATE ----------
    // @Valid = run the validation annotations on ProductRequest before we even enter the method.
    // ResponseEntity.status(CREATED).body(...) = return HTTP 201 with the new product.
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product created successfully", created));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> createProductWithImage(
            @RequestPart("product") @Valid ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ProductResponse created = productService.createProduct(request, image);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product created successfully", created));
    }

    // ---------- READ ALL ----------
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getAllProducts()));
    }

    // ---------- READ BY ID ----------
    // @PathVariable binds the {id} in the URL to this Long parameter.
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductById(id)));
    }

    // ---------- UPDATE ----------
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Product updated successfully", productService.updateProduct(id, request)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductWithImage(
            @PathVariable Long id,
            @RequestPart("product") @Valid ProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Product updated successfully", productService.updateProduct(id, request, image)));
    }

    // ---------- DELETE ----------
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        // HTTP 204 = success, no content to send back
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.ok("Product deleted successfully", null));
    }

    // ---------- SEARCH ----------
    // GET /api/products/search?keyword=laptop
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(
                ApiResponse.ok(productService.searchProducts(keyword, category)));
    }

    // ---------- FILTER BY CATEGORY ----------
    // GET /api/products/category/Electronics
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(
                ApiResponse.ok(productService.getProductsByCategory(category)));
    }

    // ---------- LIST OF CATEGORIES (for the filter dropdown) ----------
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(productService.getAllCategories()));
    }
}
