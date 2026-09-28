package com.salessavvy.dto;

import java.time.LocalDateTime;

/**
 * A standard JSON envelope so every API returns the same shape.
 *
 * Success: { "success": true, "message": "...", "data": {...}, "timestamp": "..." }
 * Error:   { "success": false, "message": "...", "data": null,  "timestamp": "..." }
 *
 * The React frontend can then always read `response.data.data` and
 * `response.data.message` without guessing.
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // ---- static factories: short ways to build a success or error response ----
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Request successful", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
