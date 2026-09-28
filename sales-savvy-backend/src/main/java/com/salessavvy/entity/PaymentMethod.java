package com.salessavvy.entity;

/** Payment channels supported for order tracking. No payment gateway is configured. */
public enum PaymentMethod {
    CASH_ON_DELIVERY,
    RAZORPAY,
    UPI,
    CARD,
    BANK_TRANSFER
}
