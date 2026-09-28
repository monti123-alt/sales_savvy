package com.salessavvy.controller;

import com.salessavvy.dto.ApiResponse;
import com.salessavvy.dto.OrderRequest;
import com.salessavvy.dto.OrderResponse;
import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.User;
import com.salessavvy.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal User user) {
        OrderResponse created = orderService.createCustomerOrder(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order created successfully", created));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrdersForUser(user)));
    }

    @GetMapping("/my/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getMyOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderForUser(id, user)));
    }

    // ---------- CREATE ----------
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse created = orderService.createOrder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order created successfully", created));
    }

    // ---------- READ ALL ----------
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders()));
    }

    // ---------- READ BY ID ----------
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderById(id)));
    }

    // ---------- ORDERS OF ONE CUSTOMER ----------
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByCustomer(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrdersByCustomer(customerId)));
    }

    // ---------- FILTER BY STATUS ----------
    // GET /api/orders/status/CREATED
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(
            @PathVariable OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrdersByStatus(status)));
    }

    // ---------- UPDATE STATUS ----------
    // PATCH is the right verb here: we are updating only ONE field, not the whole resource.
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String statusValue = body.get("status");
        if (statusValue == null || statusValue.isBlank()) {
            throw new IllegalArgumentException("'status' field is required");
        }

        // Spring converts the String to the enum automatically.
        // If someone sends "PENDING", it throws -> caught by the advice -> HTTP 400.
        OrderStatus newStatus = OrderStatus.valueOf(statusValue.toUpperCase());

        return ResponseEntity.ok(
                ApiResponse.ok("Order status updated to " + newStatus,
                        orderService.updateOrderStatus(id, newStatus)));
    }

    // Record a payment received outside the app (cash, bank transfer, or provider confirmation).
    @PatchMapping("/{id}/payment/received")
    public ResponseEntity<ApiResponse<OrderResponse>> markPaymentReceived(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Payment recorded successfully",
                orderService.markPaymentReceived(id)));
    }

    // ---------- DELETE ----------
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.ok("Order deleted successfully", null));
    }
}
