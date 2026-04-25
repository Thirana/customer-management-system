package com.example.customermanagement.exception;

import org.springframework.http.HttpStatus;

// Base exception for expected API failures that should map to a specific HTTP status.
public class ApplicationException extends RuntimeException {

    private final HttpStatus status;

    public ApplicationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
