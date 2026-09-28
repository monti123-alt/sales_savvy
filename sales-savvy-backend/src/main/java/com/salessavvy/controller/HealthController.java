package com.salessavvy.controller;

import com.salessavvy.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A public endpoint that needs no token. Useful to confirm the app is running
 * and the database connection is working.
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("application", "Sales Savvy API");
        body.put("serverTime", LocalDateTime.now());
        return ApiResponse.ok(body);
    }
}
