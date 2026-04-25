package com.example.customermanagement.dto.response;

import java.time.LocalDateTime;

// Standard envelope used by controllers and exception handlers for predictable API responses.
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, T data, String message, LocalDateTime timestamp) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<T>(true, data, message, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<T>(false, null, message, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> failure(T data, String message) {
        return new ApiResponse<T>(false, data, message, LocalDateTime.now());
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
