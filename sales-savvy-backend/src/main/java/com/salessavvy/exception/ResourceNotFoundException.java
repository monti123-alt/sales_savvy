package com.salessavvy.exception;

/**
 * Thrown when a record does not exist (e.g. product id 999).
 * The @ControllerAdvice in Phase 6 turns this into HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /** Convenience: build "Product not found with id : 5" */
    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException(resource + " not found with id : " + id);
    }
}
