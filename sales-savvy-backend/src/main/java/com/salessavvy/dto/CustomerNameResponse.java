package com.salessavvy.dto;

import com.salessavvy.entity.Customer;

public record CustomerNameResponse(Long id, String name) {

    public static CustomerNameResponse from(Customer customer) {
        return new CustomerNameResponse(customer.getId(), customer.getName());
    }
}
