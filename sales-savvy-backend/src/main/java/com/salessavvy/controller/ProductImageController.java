package com.salessavvy.controller;

import com.salessavvy.service.ProductImageStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@RestController
public class ProductImageController {

    private final ProductImageStorageService imageStorageService;

    public ProductImageController(ProductImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @GetMapping("/api/product-images/{filename:.+}")
    public ResponseEntity<Resource> getProductImage(@PathVariable String filename) throws IOException {
        Path imagePath = imageStorageService.resolve(filename);
        String extension = filename.substring(filename.lastIndexOf('.') + 1);
        MediaType mediaType = switch (extension) {
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg" -> MediaType.IMAGE_JPEG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> throw new IllegalArgumentException("Unsupported product image type");
        };
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(imagePath));
    }
}
