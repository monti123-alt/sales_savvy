package com.salessavvy.controller;

import com.salessavvy.dto.ApiResponse;
import com.salessavvy.dto.OrderResponse;
import com.salessavvy.dto.RazorpayVerifyRequest;
import com.salessavvy.entity.User;
import com.salessavvy.service.RazorpayPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/razorpay")
public class RazorpayPaymentController {

    private final RazorpayPaymentService paymentService;

    public RazorpayPaymentController(RazorpayPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCheckout(
            @PathVariable Long orderId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok("Razorpay checkout created",
                paymentService.createCheckout(orderId, user)));
    }

    @PostMapping("/orders/{orderId}/verify")
    public ResponseEntity<ApiResponse<OrderResponse>> verifyPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody RazorpayVerifyRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok("Payment verified successfully",
                paymentService.verifyPayment(orderId, request, user)));
    }
}
