package com.example.customermanagement.exception;

import org.springframework.http.HttpStatus;

public class DuplicateNicException extends ApplicationException {

    public DuplicateNicException() {
        super(HttpStatus.CONFLICT, "A customer already exists with the provided NIC number.");
    }
}
