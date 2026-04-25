package com.example.customermanagement.exception;

import org.springframework.http.HttpStatus;

public class InvalidFileException extends ApplicationException {

    public InvalidFileException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
