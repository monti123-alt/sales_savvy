package com.salessavvy.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates and verifies JWT (JSON Web Tokens).
 *
 * A JWT is a signed, self-contained ID card. It contains:
 *   header.payload.signature
 * The server signs it with a SECRET KEY, so nobody can forge one.
 * Because the server keeps no session, this is called "stateless" auth:
 * every request is verified on its own.
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        // HS256 requires a key of at least 256 bits (32 characters).
        // If the configured secret is shorter we pad it, otherwise Keys.hmacShaKeyFor throws.
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(secretBytes, 0, padded, 0, secretBytes.length);
            for (int i = secretBytes.length; i < 32; i++) {
                padded[i] = (byte) ('0' + (i % 10));
            }
            secretBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Build a token for a user.
     * The claims (payload) are the readable facts about the user.
     */
    public String generateToken(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)          // sign it
                .compact();              // turn it into a string
    }

    /** Returns the claims if the token is valid, or null if it is expired/forged. */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)     // check the signature with our secret
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            // ExpiredJwtException, SignatureException, MalformedJwtException all land here.
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        Number userId = claims.get("userId", Number.class);
        return userId == null ? null : userId.longValue();
    }
}
