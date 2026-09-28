package com.salessavvy.service;

import com.salessavvy.dto.CustomerRequest;
import com.salessavvy.dto.CustomerNameResponse;
import com.salessavvy.dto.CustomerResponse;
import com.salessavvy.entity.Customer;
import com.salessavvy.exception.CustomException;
import com.salessavvy.exception.ResourceNotFoundException;
import com.salessavvy.repository.CustomerRepository;
import com.salessavvy.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository,
                           OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    // ---------- CREATE ----------
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        // Note: we allow the same email for different customers (a family can share
        // one email), so we do NOT reject duplicates here - unlike SKU which must be unique.

        Customer customer = new Customer();
        applyRequest(customer, request);

        Customer saved = customerRepository.save(customer);
        return CustomerResponse.from(saved);
    }

    // ---------- READ ----------
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::from)
                .collect(Collectors.toList());
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
        return CustomerResponse.from(customer);
    }

    public List<CustomerResponse> searchCustomers(String keyword) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        if (kw == null) {
            return getAllCustomers();
        }
        return customerRepository.searchCustomers(kw).stream()
                .map(CustomerResponse::from)
                .collect(Collectors.toList());
    }

    public List<CustomerNameResponse> searchCustomerNames(String keyword) {
        String name = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        List<Customer> customers = name == null
                ? customerRepository.findAll()
                : customerRepository.findByNameContainingIgnoreCaseOrderByNameAsc(name);
        return customers.stream()
                .map(CustomerNameResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerNameResponse updateCustomerName(Long id, String name) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
        customer.setName(name.trim());
        return CustomerNameResponse.from(customerRepository.save(customer));
    }

    // ---------- UPDATE ----------
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", id));

        applyRequest(customer, request);

        Customer saved = customerRepository.save(customer);
        return CustomerResponse.from(saved);
    }

    // ---------- DELETE ----------
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", id));

        // Business rule: a customer with orders cannot be deleted, because orders
        // reference them. We could either block or cascade-delete the orders.
        // Blocking is safer for accounting - we don't want to erase sales history.
        long orderCount = orderRepository.findByCustomerId(id).size();
        if (orderCount > 0) {
            throw new CustomException(
                    "Customer has " + orderCount + " order(s) and cannot be deleted.");
        }

        customerRepository.delete(customer);
    }

    /**
     * Copy the fields from the request onto the entity.
     * Reused by both create and update so the two stay consistent.
     */
    private void applyRequest(Customer customer, CustomerRequest request) {
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPostalCode(request.getPostalCode());
        customer.setCountry(request.getCountry());
    }
}
