package com.salessavvy.controller;

import com.salessavvy.dto.ApiResponse;
import com.salessavvy.dto.CustomerRequest;
import com.salessavvy.dto.CustomerNameResponse;
import com.salessavvy.dto.CustomerNameUpdateRequest;
import com.salessavvy.dto.CustomerResponse;
import com.salessavvy.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.createCustomer(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Customer created successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getAllCustomers()));
    }

    @GetMapping("/names")
    public ResponseEntity<ApiResponse<List<CustomerNameResponse>>> getCustomerNames(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.searchCustomerNames(keyword)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getCustomerById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Customer updated successfully", customerService.updateCustomer(id, request)));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<ApiResponse<CustomerNameResponse>> updateCustomerName(
            @PathVariable Long id,
            @Valid @RequestBody CustomerNameUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Customer name updated successfully",
                        customerService.updateCustomerName(id, request.name())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.ok("Customer deleted successfully", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> searchCustomers(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.searchCustomers(keyword)));
    }
}
