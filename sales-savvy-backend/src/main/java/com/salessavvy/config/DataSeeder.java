package com.salessavvy.config;

import com.salessavvy.entity.*;
import com.salessavvy.repository.CustomerRepository;
import com.salessavvy.repository.ProductRepository;
import com.salessavvy.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Puts a few demo rows into the database the first time the app starts,
 * so the dashboard and Postman have something to show.
 *
 * Each block is guarded by a count check, so re-running never duplicates data.
 */
@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(UserRepository userRepository,
                                      CustomerRepository customerRepository,
                                      ProductRepository productRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {

            // ---- 1. A default login ----
            if (userRepository.count() == 0) {
                User admin = new User("Admin User", "admin@salessavvy.com",
                        passwordEncoder.encode("admin123"), Role.ADMIN);
                userRepository.save(admin);

                User user = new User("Sales Rep", "user@salessavvy.com",
                        passwordEncoder.encode("user123"), Role.USER);
                userRepository.save(user);
                System.out.println(">>> Seeded users: admin@salessavvy.com / admin123");
            }

            // ---- 2. Demo customers ----
            if (customerRepository.count() == 0) {
                customerRepository.saveAll(List.of(
                        newCustomer("Ravi Sharma", "ravi@example.com", "9876543210",
                                "12 MG Road", "Bengaluru", "Karnataka", "560001", "India"),
                        newCustomer("Priya Nair", "priya@example.com", "9876543211",
                                "45 Park Street", "Kolkata", "West Bengal", "700001", "India"),
                        newCustomer("Amit Patel", "amit@example.com", "9876543212",
                                "78 Linking Road", "Mumbai", "Maharashtra", "400001", "India")
                ));
                System.out.println(">>> Seeded 3 customers");
            }

            // ---- 3. Demo products ----
            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                        newProduct("Laptop Pro 14", "LAP-001", "Electronics",
                                new BigDecimal("74999.00"), 12,
                                "14-inch ultrabook with 16GB RAM and 512GB SSD"),
                        newProduct("Wireless Mouse", "MOU-001", "Accessories",
                                new BigDecimal("899.00"), 45,
                                "Ergonomic wireless mouse with USB-C receiver"),
                        newProduct("USB-C Cable 1m", "CAB-001", "Accessories",
                                new BigDecimal("349.00"), 4,
                                "Fast charging braided cable"),
                        newProduct("Noise Cancelling Headphones", "AUD-001", "Electronics",
                                new BigDecimal("12999.00"), 20,
                                "Over-ear ANC headphones with 30h battery"),
                        newProduct("Notebook A5", "STA-001", "Stationery",
                                new BigDecimal("249.00"), 100,
                                "Hardbound ruled notebook, 200 pages"),
                        newProduct("Desk Lamp LED", "HOM-001", "Home",
                                new BigDecimal("1199.00"), 3,
                                "Adjustable colour temperature desk lamp")
                ));
                System.out.println(">>> Seeded 6 products");
            }

            List<Product> additionalProducts = List.of(
                    newProduct("Bluetooth Speaker 10W", "AUD-002", "Electronics",
                            new BigDecimal("1799.00"), 24, "Portable wireless speaker with a 10W driver and USB-C charging"),
                    newProduct("Wireless Keyboard", "ACC-002", "Accessories",
                            new BigDecimal("1499.00"), 32, "Slim wireless keyboard with a compact number pad"),
                    newProduct("USB-C Hub 6-in-1", "ACC-003", "Accessories",
                            new BigDecimal("2299.00"), 18, "Aluminium USB-C hub with HDMI, USB-A, and card-reader ports"),
                    newProduct("Laptop Stand Adjustable", "ACC-004", "Accessories",
                            new BigDecimal("1299.00"), 27, "Foldable aluminium laptop stand with adjustable viewing angles"),
                    newProduct("Fast Charger 30W", "ACC-005", "Accessories",
                            new BigDecimal("999.00"), 38, "Compact 30W USB-C wall charger for compatible devices"),
                    newProduct("Power Bank 10000mAh", "ACC-006", "Accessories",
                            new BigDecimal("1899.00"), 21, "Pocket-size 10000mAh power bank with USB-C input and output"),
                    newProduct("Cotton Bath Towel Set of 2", "HOM-002", "Home",
                            new BigDecimal("899.00"), 42, "Two soft, absorbent cotton bath towels"),
                    newProduct("Insulated Steel Water Bottle 750ml", "HOM-003", "Home",
                            new BigDecimal("699.00"), 36, "Reusable stainless steel bottle with a leak-resistant cap"),
                    newProduct("Ceramic Mug Set of 2", "HOM-004", "Home",
                            new BigDecimal("549.00"), 29, "Pair of everyday glazed ceramic coffee mugs"),
                    newProduct("Cotton Cushion Cover Set of 2", "HOM-005", "Home",
                            new BigDecimal("649.00"), 31, "Pair of textured cotton cushion covers with concealed zips"),
                    newProduct("LED Desk Light with USB", "HOM-006", "Home",
                            new BigDecimal("899.00"), 22, "Dimmable LED desk light with a flexible neck and USB power"),
                    newProduct("Microfiber Bedsheet Double", "HOM-007", "Home",
                            new BigDecimal("1199.00"), 17, "Easy-care double bedsheet with two matching pillow covers"),
                    newProduct("Hardcover Journal A5", "STA-002", "Stationery",
                            new BigDecimal("399.00"), 48, "A5 ruled journal with a ribbon bookmark and elastic closure"),
                    newProduct("Gel Pen Set of 10", "STA-003", "Stationery",
                            new BigDecimal("249.00"), 65, "Set of ten smooth-writing assorted-colour gel pens"),
                    newProduct("Weekly Planner Notebook", "STA-004", "Stationery",
                            new BigDecimal("349.00"), 40, "Undated weekly planner with space for notes and priorities"),
                    newProduct("Canvas Tote Bag", "FAS-001", "Fashion",
                            new BigDecimal("499.00"), 34, "Reusable cotton canvas tote bag with reinforced handles"),
                    newProduct("Classic Analog Wristwatch", "FAS-002", "Fashion",
                            new BigDecimal("1599.00"), 15, "Everyday analog wristwatch with a simple dial and adjustable strap"),
                    newProduct("Yoga Mat 6mm", "SPT-001", "Sports",
                            new BigDecimal("999.00"), 26, "Non-slip 6mm exercise mat for stretching and floor workouts"),
                    newProduct("Resistance Bands Set of 5", "SPT-002", "Sports",
                            new BigDecimal("449.00"), 39, "Five resistance levels for home exercise and mobility work"),
                    newProduct("Gentle Face Moisturizer 100ml", "BEA-001", "Beauty",
                            new BigDecimal("399.00"), 28, "Lightweight daily face moisturizer in a 100ml bottle")
            );
            List<Product> productsToAdd = additionalProducts.stream()
                    .filter(product -> !productRepository.existsBySku(product.getSku()))
                    .toList();
            if (!productsToAdd.isEmpty()) {
                productRepository.saveAll(productsToAdd);
                System.out.println(">>> Added " + productsToAdd.size() + " additional catalog products");
            }
        };
    }

    private Product newProduct(String name, String sku, String category,
                               BigDecimal price, int stock, String description) {
        Product p = new Product(name, sku, category, price, stock);
        p.setDescription(description);
        return p;
    }

    private Customer newCustomer(String name, String email, String phone,
                                 String address, String city, String state,
                                 String postalCode, String country) {
        Customer c = new Customer(name, email, phone, address);
        c.setCity(city);
        c.setState(state);
        c.setPostalCode(postalCode);
        c.setCountry(country);
        return c;
    }
}
