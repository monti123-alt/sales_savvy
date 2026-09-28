package com.salessavvy.dto;

import com.salessavvy.entity.Role;
import com.salessavvy.entity.User;

/**
 * What we return after a successful login.
 *
 * The JWT token is given to the client, which then sends it in the
 * "Authorization: Bearer <token>" header on every later request.
 *
 * Note: there is NO password field here. It must never leave the server.
 */
public class AuthResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private String token;

    public AuthResponse() {
    }

    public AuthResponse(Long id, String name, String email, Role role, String token) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public static AuthResponse from(User user, String token) {
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), token);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public String getToken() { return token; }
}
