package com.example.customermanagement.exception;

import org.springframework.http.HttpStatus;

public class CustomerNotFoundException extends ApplicationException {

    public CustomerNotFoundException(Long customerId) {
        super(HttpStatus.NOT_FOUND, "Customer not found for id: " + customerId);
    }
}
