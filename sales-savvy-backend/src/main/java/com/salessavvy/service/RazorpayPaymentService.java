package com.salessavvy.service;

import com.salessavvy.dto.OrderResponse;
import com.salessavvy.dto.RazorpayVerifyRequest;
import com.salessavvy.entity.Order;
import com.salessavvy.entity.PaymentMethod;
import com.salessavvy.entity.PaymentStatus;
import com.salessavvy.entity.Role;
import com.salessavvy.entity.User;
import com.salessavvy.exception.CustomException;
import com.salessavvy.exception.ResourceNotFoundException;
import com.salessavvy.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RazorpayPaymentService {

    private static final String API_URL = "https://api.razorpay.com/v1";

    private final OrderRepository orderRepository;
    private final RestClient restClient;
    private final String keyId;
    private final String keySecret;

    public RazorpayPaymentService(
            OrderRepository orderRepository,
            @Value("${razorpay.key-id:}") String keyId,
            @Value("${razorpay.key-secret:}") String keySecret) {
        this.orderRepository = orderRepository;
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.restClient = RestClient.builder().baseUrl(API_URL).build();
    }

    @Transactional
    public Map<String, Object> createCheckout(Long orderId, User user) {
        Order order = findOrderForUser(orderId, user);
        requireConfigured();
        if (order.getPaymentMethod() != PaymentMethod.RAZORPAY) {
            throw new CustomException("This order is not set up for Razorpay checkout");
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new CustomException("This order has already been paid");
        }
        if (order.getStatus().name().equals("CANCELLED")) {
            throw new CustomException("Payment cannot be started for a cancelled order");
        }

        long amountPaise = order.getTotalAmount()
                .movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();
        if (amountPaise <= 0) {
            throw new CustomException("Razorpay requires an order amount greater than zero");
        }

        if (order.getGatewayOrderId() == null || order.getGatewayOrderId().isBlank()) {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("amount", amountPaise);
            request.put("currency", "INR");
            request.put("receipt", order.getOrderNumber());

            Map<?, ?> response = callRazorpay("/orders", request);
            Object gatewayOrderId = response.get("id");
            if (!(gatewayOrderId instanceof String id) || id.isBlank()) {
                throw new CustomException("Razorpay did not return an order reference");
            }
            order.setGatewayOrderId(id);
            orderRepository.save(order);
        }

        return Map.of(
                "keyId", keyId,
                "razorpayOrderId", order.getGatewayOrderId(),
                "amount", amountPaise,
                "currency", "INR"
        );
    }

    @Transactional
    public OrderResponse verifyPayment(Long orderId, RazorpayVerifyRequest request, User user) {
        Order order = findOrderForUser(orderId, user);
        requireConfigured();
        if (order.getPaymentMethod() != PaymentMethod.RAZORPAY
                || order.getGatewayOrderId() == null
                || !order.getGatewayOrderId().equals(request.getRazorpayOrderId())) {
            throw new CustomException("The payment does not match this order");
        }
        if (order.getStatus().name().equals("CANCELLED")) {
            throw new CustomException("Payment cannot be verified for a cancelled order");
        }

        verifySignature(request);
        Map<?, ?> payment = callRazorpay("/payments/" + request.getRazorpayPaymentId(), null);
        long expectedPaise = order.getTotalAmount().movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        if (!request.getRazorpayOrderId().equals(payment.get("order_id"))
                || !(payment.get("amount") instanceof Number amount)
                || amount.longValue() != expectedPaise
                || !"INR".equals(payment.get("currency"))
                || !"captured".equals(payment.get("status"))) {
            throw new CustomException("Razorpay has not confirmed a captured payment for this order");
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setGatewayPaymentId(request.getRazorpayPaymentId());
        return OrderResponse.from(orderRepository.save(order));
    }

    private void verifySignature(RazorpayVerifyRequest request) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            String expected = HexFormat.of().formatHex(hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                    request.getRazorpaySignature().getBytes(StandardCharsets.US_ASCII))) {
                throw new CustomException("Razorpay payment signature is invalid");
            }
        } catch (CustomException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CustomException("Could not verify the Razorpay payment signature");
        }
    }

    private Map<?, ?> callRazorpay(String path, Map<String, Object> body) {
        try {
            RestClient.RequestHeadersSpec<?> request;
            if (body == null) {
                request = restClient.get().uri(path);
            } else {
                request = restClient.post().uri(path).body(body);
            }
            Map<?, ?> response = request
                    .headers(headers -> headers.setBasicAuth(keyId, keySecret))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new CustomException("Razorpay rejected the payment request");
                    })
                    .body(Map.class);
            if (response == null) {
                throw new CustomException("Razorpay returned an empty response");
            }
            return response;
        } catch (CustomException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new CustomException("Unable to contact Razorpay. Please try again.");
        }
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));
    }

    private Order findOrderForUser(Long orderId, User user) {
        Order order = findOrder(orderId);
        if (user.getRole() != Role.ADMIN
                && !order.getCustomer().getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new CustomException("You are not allowed to access this order");
        }
        return order;
    }

    private void requireConfigured() {
        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            throw new CustomException("Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET on the backend.");
        }
    }
}
