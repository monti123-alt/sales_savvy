package com.salessavvy.dto;

import com.salessavvy.entity.Customer;

import java.time.LocalDateTime;

/**
 * What we send back for a customer.
 */
public class CustomerResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private LocalDateTime createdAt;

    /** How many orders this customer has placed (filled by the service, not the entity) */
    private Long orderCount;

    public CustomerResponse() {
    }

    public static CustomerResponse from(Customer customer) {
        if (customer == null) {
            return null;
        }
        CustomerResponse dto = new CustomerResponse();
        dto.id = customer.getId();
        dto.name = customer.getName();
        dto.email = customer.getEmail();
        dto.phone = customer.getPhone();
        dto.address = customer.getAddress();
        dto.city = customer.getCity();
        dto.state = customer.getState();
        dto.postalCode = customer.getPostalCode();
        dto.country = customer.getCountry();
        dto.createdAt = customer.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getOrderCount() { return orderCount; }
    public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }
}
