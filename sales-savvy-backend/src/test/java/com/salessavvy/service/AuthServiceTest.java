package com.salessavvy.service;

import com.salessavvy.config.JwtUtil;
import com.salessavvy.dto.ChangePasswordRequest;
import com.salessavvy.entity.Role;
import com.salessavvy.entity.User;
import com.salessavvy.exception.CustomException;
import com.salessavvy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authService = new AuthService(userRepository, passwordEncoder, mock(JwtUtil.class));
    }

    @Test
    void changePasswordUpdatesHashAndInvalidatesExistingTokens() {
        User user = new User("Store Admin", "admin@example.com", "old-hash", Role.ADMIN);
        user.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-password-long", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-password-long")).thenReturn("new-hash");

        authService.changePassword(user, request("old-password", "new-password-long", "new-password-long"));

        assertEquals("new-hash", user.getPassword());
        assertEquals(1L, user.getTokenVersion());
        verify(userRepository).save(user);
    }

    @Test
    void changePasswordRejectsAnIncorrectCurrentPasswordWithoutUpdating() {
        User user = new User("Store Admin", "admin@example.com", "old-hash", Role.ADMIN);
        user.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

        assertThrows(CustomException.class, () -> authService.changePassword(
                user, request("wrong-password", "new-password-long", "new-password-long")));

        verify(userRepository, never()).save(any(User.class));
        assertEquals("old-hash", user.getPassword());
        assertEquals(0L, user.getTokenVersion());
    }

    @Test
    void changePasswordRejectsMismatchedConfirmation() {
        User user = new User("Store Admin", "admin@example.com", "old-hash", Role.ADMIN);
        user.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);

        assertThrows(CustomException.class, () -> authService.changePassword(
                user, request("old-password", "new-password-long", "different-password")));

        verify(userRepository, never()).save(any(User.class));
    }

    private ChangePasswordRequest request(String current, String next, String confirm) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword(current);
        request.setNewPassword(next);
        request.setConfirmPassword(confirm);
        return request;
    }
}
