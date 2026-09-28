package com.salessavvy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Spring Boot application.
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan.
 * ComponentScan starts at this package, so everything under com.salessavvy is scanned.
 */
@SpringBootApplication
public class SalesSavvyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesSavvyApplication.class, args);
    }
}
