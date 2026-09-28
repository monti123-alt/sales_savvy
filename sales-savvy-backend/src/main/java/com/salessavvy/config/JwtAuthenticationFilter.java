package com.salessavvy.config;

import com.salessavvy.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Runs BEFORE every controller method.
 *
 * Its job: look at the "Authorization: Bearer <token>" header, verify the JWT,
 * and if it's valid tell Spring Security "this request is authenticated as user 1".
 * If the token is missing or bad, the request is left unauthenticated and
 * Spring Security rejects it with 401.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Pull the token out of the header
        String header = request.getHeader("Authorization");
        String token = null;

        if (header != null && header.startsWith("Bearer ")) {
            // "Authorization: Bearer eyJhbGciOi..." -> strip the "Bearer " prefix
            token = header.substring(7);
        }

        // 2. If we have a token, verify it
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            Claims claims = jwtUtil.validateToken(token);

            if (claims != null) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String email = claims.get("email", String.class);
                String role = claims.get("role", String.class);
                Number tokenVersion = claims.get("tokenVersion", Number.class);

                // 3. Make sure the user still exists and is enabled in the database.
                //    Without this check, a deleted user's token would keep working
                //    until it expires.
                userRepository.findById(userId)
                        .filter(user -> user.isEnabled()
                                && user.getEmail().equals(email)
                                && user.getTokenVersion() == (tokenVersion == null ? 0 : tokenVersion.longValue()))
                        .ifPresent(user -> {
                            // 4. Tell Spring Security who this request is from.
                            //    ROLE_ prefix is Spring's convention for authorities.
                            List<SimpleGrantedAuthority> authorities = List.of(
                                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            user,                 // principal (who)
                                            null,                 // credentials (none - already verified)
                                            authorities);         // what they're allowed to do

                            authentication.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            }
        }

        // 5. Continue the chain either way. If we're not authenticated,
        //    the security config will return 401 for protected URLs.
        filterChain.doFilter(request, response);
    }
}
