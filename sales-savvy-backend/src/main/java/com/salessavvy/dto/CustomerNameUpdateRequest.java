package com.salessavvy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerNameUpdateRequest(
        @NotBlank(message = "Customer name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name) {
}
