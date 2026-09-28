package com.salessavvy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ProductImageStorageService {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private final Path imageDirectory;

    public ProductImageStorageService(
            @Value("${salessavvy.upload-dir:uploads}") String uploadDirectory) {
        this.imageDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize().resolve("products");
    }

    public String store(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Choose an image to upload");
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("Product image must be 5 MB or smaller");
        }

        try {
            byte[] bytes = image.getBytes();
            String extension = detectImageExtension(bytes);
            String filename = UUID.randomUUID() + extension;
            Files.createDirectories(imageDirectory);
            Files.write(imageDirectory.resolve(filename), bytes);
            return "/api/product-images/" + filename;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save product image", ex);
        }
    }

    public Path resolve(String filename) {
        if (filename == null || !filename.matches("[a-f0-9-]{36}\\.(png|jpg|gif|webp)")) {
            throw new IllegalArgumentException("Invalid product image name");
        }
        Path image = imageDirectory.resolve(filename).normalize();
        if (!image.startsWith(imageDirectory) || !Files.isRegularFile(image)) {
            throw new IllegalArgumentException("Product image was not found");
        }
        return image;
    }

    private String detectImageExtension(byte[] bytes) {
        if (bytes.length >= 8
                && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e
                && bytes[3] == 0x47 && bytes[4] == 0x0d && bytes[5] == 0x0a
                && bytes[6] == 0x1a && bytes[7] == 0x0a) {
            return ".png";
        }
        if (bytes.length >= 3
                && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff) {
            return ".jpg";
        }
        if (bytes.length >= 6
                && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F'
                && bytes[3] == '8' && (bytes[4] == '7' || bytes[4] == '9')
                && bytes[5] == 'a') {
            return ".gif";
        }
        if (bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return ".webp";
        }
        throw new IllegalArgumentException("Upload a valid PNG, JPEG, GIF, or WebP image");
    }
}
