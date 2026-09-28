package com.salessavvy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS = Cross-Origin Resource Sharing.
 *
 * The problem: our React app runs on http://localhost:5173 and the API runs
 * on http://localhost:8080. Different ports = different origins, so the
 * browser BLOCKS the request by default. Enabling CORS tells the browser
 * "requests from localhost:5173 are allowed".
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600);   // cache the preflight for 1 hour
    }
}
