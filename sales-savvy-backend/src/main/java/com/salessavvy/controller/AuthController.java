package com.salessavvy.controller;

import com.salessavvy.config.JwtUtil;
import com.salessavvy.dto.ApiResponse;
import com.salessavvy.dto.AuthResponse;
import com.salessavvy.dto.LoginRequest;
import com.salessavvy.dto.RegisterRequest;
import com.salessavvy.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // ---------- REGISTER : POST /api/auth/register ----------
    @PostMapping("/api/auth/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful", response));
    }

    // ---------- LOGIN : POST /api/auth/login ----------
    @PostMapping("/api/auth/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    // ---------- WHO AM I : GET /api/auth/me (token required) ----------
    @GetMapping("/api/auth/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> currentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> info = new LinkedHashMap<>();

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            Claims claims = jwtUtil.validateToken(authHeader.substring(7));
            if (claims != null) {
                // Read the facts back out of the token's payload
                info.put("userId", claims.get("userId", Number.class).longValue());
                info.put("email", claims.get("email", String.class));
                info.put("role", claims.get("role", String.class));
                info.put("authenticated", true);
                return ResponseEntity.ok(ApiResponse.ok(info));
            }
        }

        info.put("authenticated", false);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid or missing token"));
    }
}
