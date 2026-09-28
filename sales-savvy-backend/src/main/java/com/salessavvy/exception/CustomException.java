package com.salessavvy.exception;

/**
 * Thrown for business rule violations that are not "not found",
 * e.g. duplicate SKU, insufficient stock, deleting a product that is used in orders.
 * The @ControllerAdvice turns this into HTTP 400.
 */
public class CustomException extends RuntimeException {

    public CustomException(String message) {
        super(message);
    }
}
