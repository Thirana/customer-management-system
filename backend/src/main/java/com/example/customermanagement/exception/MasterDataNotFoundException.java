package com.example.customermanagement.exception;

import org.springframework.http.HttpStatus;

public class MasterDataNotFoundException extends ApplicationException {

    public MasterDataNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
