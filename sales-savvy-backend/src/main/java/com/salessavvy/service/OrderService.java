package com.salessavvy.service;

import com.salessavvy.dto.OrderItemRequest;
import com.salessavvy.dto.OrderRequest;
import com.salessavvy.dto.OrderResponse;
import com.salessavvy.entity.Customer;
import com.salessavvy.entity.Order;
import com.salessavvy.entity.OrderItem;
import com.salessavvy.entity.OrderStatus;
import com.salessavvy.entity.Product;
import com.salessavvy.entity.User;
import com.salessavvy.exception.CustomException;
import com.salessavvy.exception.ResourceNotFoundException;
import com.salessavvy.repository.CustomerRepository;
import com.salessavvy.repository.OrderRepository;
import com.salessavvy.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final DateTimeFormatter ORDER_NUMBER_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    // ================= CREATE ORDER =================
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        if (request.getCustomerId() == null) {
            throw new CustomException("Customer id is required");
        }
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", request.getCustomerId()));
        return createOrderForCustomer(request, customer);
    }

    @Transactional
    public OrderResponse createCustomerOrder(OrderRequest request, User user) {
        Customer customer = customerRepository.findFirstByEmailIgnoreCaseOrderByIdAsc(user.getEmail())
                .orElseGet(() -> {
                    Customer created = new Customer(user.getName(), user.getEmail(), null, null);
                    return customerRepository.save(created);
                });
        return createOrderForCustomer(request, customer);
    }

    private OrderResponse createOrderForCustomer(OrderRequest request, Customer customer) {

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.CREATED);
        order.setPaymentMethod(request.getPaymentMethod() != null
            ? request.getPaymentMethod()
            : com.salessavvy.entity.PaymentMethod.CASH_ON_DELIVERY);
        order.setPaymentStatus(com.salessavvy.entity.PaymentStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(
                request.getShippingAddress() != null && !request.getShippingAddress().isBlank()
                        ? request.getShippingAddress()
                        : customer.getAddress());   // fall back to the customer's saved address
        order.setNotes(request.getNotes());
        order.setTotalAmount(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            // look up the REAL product - never trust a price from the client
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Product", itemRequest.getProductId()));

            if (!product.isActive()) {
                throw new CustomException("Product '" + product.getName() + "' is discontinued and cannot be ordered");
            }

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new CustomException(
                        "Insufficient stock for '" + product.getName() + "'. " +
                        "Requested " + itemRequest.getQuantity() +
                        " but only " + product.getStockQuantity() + " available");
            }

            // the OrderItem constructor snapshots name + price and computes lineTotal
            OrderItem item = new OrderItem(product, itemRequest.getQuantity());
            order.addItem(item);   // also sets item.setOrder(this)

            total = total.add(item.getLineTotal());

            // reduce stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));

        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    // ================= READ =================
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", id));
        return OrderResponse.from(order);
    }

    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersForUser(User user) {
        return orderRepository.findByCustomerEmail(user.getEmail()).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderForUser(Long id, User user) {
        Order order = orderRepository.findByIdAndCustomerEmail(id, user.getEmail())
                .orElseThrow(() -> ResourceNotFoundException.of("Order", id));
        return OrderResponse.from(order);
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByOrderDateDesc(status).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    // ================= UPDATE STATUS =================
    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", id));

        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == newStatus) {
            return OrderResponse.from(order);   // nothing to do
        }

        // Business rule: an order can only move forward through the lifecycle.
        if (!isValidTransition(oldStatus, newStatus)) {
            throw new CustomException(
                    "Cannot change order status from " + oldStatus + " to " + newStatus);
        }

        // Cancelling an order puts the stock back on the shelf.
        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        return OrderResponse.from(saved);
    }

    // ================= RECORD PAYMENT =================
    @Transactional
    public OrderResponse markPaymentReceived(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new CustomException("Payment cannot be recorded for a cancelled order");
        }
        if (order.getPaymentMethod() != com.salessavvy.entity.PaymentMethod.CASH_ON_DELIVERY) {
            throw new CustomException("Online payments must be confirmed by the payment gateway");
        }

        order.setPaymentStatus(com.salessavvy.entity.PaymentStatus.PAID);
        return OrderResponse.from(orderRepository.save(order));
    }

    // ================= DELETE =================
    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", id));

        // Put the stock back before deleting, otherwise the inventory drifts over time.
        if (order.getStatus() != OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        // Items are removed automatically thanks to cascade + orphanRemoval
        orderRepository.delete(order);
    }

    // ================= TOTAL AMOUNT =================
    /** Recalculate the total from the individual line totals. */
    public BigDecimal calculateTotal(Order order) {
        return order.getItems().stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ================= HELPERS =================

    /**
     * Generates a unique order number like "ORD-20260928-0007".
     * It uses the current date plus a per-day counter.
     */
    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(ORDER_NUMBER_FORMAT);
        String prefix = "ORD-" + datePart + "-";

        // How many orders already exist today with this prefix?
        long todayCount = orderRepository.findAll().stream()
                .filter(o -> o.getOrderNumber() != null && o.getOrderNumber().startsWith(prefix))
                .count();

        String candidate;
        do {
            todayCount++;
            // %04d = zero-padded number, e.g. 7 -> "0007"
            candidate = prefix + String.format("%04d", todayCount);
        } while (orderRepository.existsByOrderNumber(candidate));

        return candidate;
    }

    /** Put every ordered item's quantity back into stock. */
    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    /**
     * Allowed status moves:
     * CREATED   -> CONFIRMED, CANCELLED
     * CONFIRMED -> SHIPPED, CANCELLED
     * SHIPPED   -> DELIVERED
     * DELIVERED -> (terminal, nothing allowed)
     * CANCELLED -> (terminal)
     */
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case CREATED   -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.SHIPPED  || to == OrderStatus.CANCELLED;
            case SHIPPED   -> to == OrderStatus.DELIVERED;
            case DELIVERED -> false;
            case CANCELLED -> false;
        };
    }
}
