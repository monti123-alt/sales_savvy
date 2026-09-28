package com.salessavvy.exception;

import com.salessavvy.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ONE place that turns every exception into a clean JSON error response.
 *
 * Why? Without this, the client receives Spring's default error page (a big
 * HTML/JSON blob) and has to guess what went wrong. With @RestControllerAdvice,
 * EVERY failure looks like:
 *   { "success": false, "message": "human readable reason", ... }
 *
 * @RestControllerAdvice = @ControllerAdvice that returns JSON (not view names)
 *                         and applies to ALL controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 404 : something does not exist ----------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ---------- 400 : business rule violated ----------
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustom(CustomException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ---------- 400 : validation failed (@Valid) ----------
    // MethodArgumentNotValidException is thrown when a @Valid request body fails its checks.
    // We return EVERY failing field at once, so the user can fix them all in one go
    // instead of discovering them one by one.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("message", "Validation failed");
        body.put("errors", fieldErrors);
        body.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ---------- 400 : malformed JSON ----------
    // e.g. the client sends { "name": } with no value, or "price": "abc"
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request body");
    }

    // ---------- 400 : wrong parameter type ----------
    // e.g. GET /api/products/abc - "abc" cannot be parsed as a Long
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + ex.getName() + "'");
    }

    // ---------- 400 : bad argument (e.g. an invalid enum value) ----------
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleIllegal(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiResponse.error(ex.getReason() == null ? "Request failed" : ex.getReason()));
    }

    // ---------- 401 : bad or missing credentials ----------
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex) {
        return build(HttpStatus.UNAUTHORIZED, "You are not authorised to perform this action");
    }

    // ---------- 500 : a genuine bug. Log the details, hide them from the user ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        // Log it for the developer (with the stack trace)
        log.error("Unexpected error occurred", ex);
        // But do NOT leak internals to the client
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
    }

    // ---------- helper ----------
    private ResponseEntity<ApiResponse<Void>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(message));
    }
}
