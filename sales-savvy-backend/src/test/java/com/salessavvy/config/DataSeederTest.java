package com.salessavvy.config;

import com.salessavvy.entity.Role;
import com.salessavvy.entity.User;
import com.salessavvy.repository.CustomerRepository;
import com.salessavvy.repository.ProductRepository;
import com.salessavvy.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataSeederTest {

    @Test
    void demoDataModeDisablesLegacyAccountsWithoutCreatingSharedLogins() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User demoCustomer = new User("Sales Rep", "user@salessavvy.com", "hash", Role.USER);
        User demoAdmin = new User("Admin User", "admin@salessavvy.com", "hash", Role.ADMIN);

        when(userRepository.findByEmail("user@salessavvy.com")).thenReturn(Optional.of(demoCustomer));
        when(userRepository.findByEmail("admin@salessavvy.com")).thenReturn(Optional.of(demoAdmin));
        when(userRepository.existsByRoleAndEnabledTrue(Role.ADMIN)).thenReturn(false);
        when(customerRepository.count()).thenReturn(1L);
        when(productRepository.count()).thenReturn(1L);
        when(productRepository.existsBySku(anyString())).thenReturn(true);

        new DataSeeder().seedData(userRepository, customerRepository, productRepository,
                passwordEncoder, true, "", "", "").run();

        assertFalse(demoCustomer.isEnabled());
        assertFalse(demoAdmin.isEnabled());
        assertEquals(1L, demoCustomer.getTokenVersion());
        assertEquals(1L, demoAdmin.getTokenVersion());
        verify(userRepository, times(2)).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void productionModeBootstrapsConfiguredAdminAndDisablesLegacyLogins() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User demoAdmin = new User("Admin User", "admin@salessavvy.com", "hash", Role.ADMIN);

        when(userRepository.findByEmail("user@salessavvy.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@salessavvy.com")).thenReturn(Optional.of(demoAdmin));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByRoleAndEnabledTrue(Role.ADMIN)).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");

        new DataSeeder().seedData(userRepository, customerRepository, productRepository,
                passwordEncoder, false, "owner@example.com", "password",
                "a-secure-jwt-secret-that-is-at-least-32-characters").run();

        assertFalse(demoAdmin.isEnabled());
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getEmail().equals("owner@example.com")
                        && user.getRole() == Role.ADMIN
                        && user.getPassword().equals("encoded-password")
                        && user.isEnabled()));
    }
}
