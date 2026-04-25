package com.example.customermanagement.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends ApplicationException {

    public InvalidRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
