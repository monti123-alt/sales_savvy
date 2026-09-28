package com.salessavvy.service;

import com.salessavvy.config.JwtUtil;
import com.salessavvy.dto.AuthResponse;
import com.salessavvy.dto.LoginRequest;
import com.salessavvy.dto.RegisterRequest;
import com.salessavvy.entity.Role;
import com.salessavvy.entity.User;
import com.salessavvy.exception.CustomException;
import com.salessavvy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ---------- REGISTER ----------
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // THE CRITICAL LINE: hash the password before it ever touches the database.
        // user.setPassword(request.getPassword())  <-- NEVER do this
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(Role.USER);     // new accounts are regular users
        user.setEnabled(true);

        User saved = userRepository.save(user);

        // Issue a token immediately so the user is logged in after registering
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name());
        return AuthResponse.from(saved, token);
    }

    // ---------- LOGIN ----------
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmailAndEnabledTrue(request.getEmail())
                .orElseThrow(() -> new CustomException("Invalid email or password"));

        // matches() compares the typed password against the stored BCrypt hash.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.from(user, token);
    }
}
