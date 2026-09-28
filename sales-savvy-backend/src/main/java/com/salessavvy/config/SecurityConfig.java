package com.salessavvy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * The security configuration - the master switchboard of Spring Security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * BCrypt = the password hashing algorithm.
     *
     * We must NEVER store passwords as-is. If our database leaks, plain passwords
     * would let attackers log in everywhere the user reused that password.
     *
     * BCrypt is slow ON PURPOSE - slowness is the whole point. A fast hash
     * (like SHA-256) can be brute-forced billions of times per second. BCrypt
     * is deliberately expensive, so a leaked hash is safe. The "10" is the
     * strength/cost factor (each +1 doubles the work).
     *
     * BCrypt also salts automatically: two users with the same password
     * get DIFFERENT hashes. That's why the column is 60 characters.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // Disable these - we are a stateless REST API using tokens, not
            // a browser app using cookies, so these built-in protections are
            // unnecessary and would interfere.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> { })   // uses the CorsConfig bean from earlier
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex ->
                    ex.authenticationEntryPoint(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

            // Which URLs need a token?
            .authorizeHttpRequests(auth -> auth
                // Public: anyone can reach these, no token needed
                .requestMatchers(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/health",
                        "/api/product-images/**"
                ).permitAll()

                .requestMatchers(HttpMethod.POST, "/api/orders/checkout").authenticated()
                .requestMatchers("/api/orders/my/**", "/api/payments/razorpay/**").authenticated()

                // Customers can browse products; only admins can change the catalog.
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").authenticated()
                .requestMatchers("/api/products", "/api/products/**").hasRole("ADMIN")

                // Customer, dashboard, and order-management data is admin-only.
                .requestMatchers("/api/customers", "/api/customers/**",
                        "/api/dashboard", "/api/dashboard/**",
                        "/api/orders", "/api/orders/**").hasRole("ADMIN")

                // Everything else under /api requires a valid JWT.
                .requestMatchers("/api/**").authenticated()

                // Any other request (e.g. /error) is allowed
                .anyRequest().permitAll()
            )

            // Run our JWT filter before Spring's default username/password filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
