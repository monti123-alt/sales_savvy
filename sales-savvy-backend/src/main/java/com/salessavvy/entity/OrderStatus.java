package com.salessavvy.entity;

/**
 * Lifecycle of an order.
 * CREATED  -> order just placed
 * CONFIRMED-> customer/payment accepted
 * SHIPPED  -> goods sent
 * DELIVERED-> goods received
 * CANCELLED-> order voided
 */
public enum OrderStatus {
    CREATED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
