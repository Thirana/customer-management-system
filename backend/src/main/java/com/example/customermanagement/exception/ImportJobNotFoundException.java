package com.example.customermanagement.exception;

import org.springframework.http.HttpStatus;

public class ImportJobNotFoundException extends ApplicationException {

    public ImportJobNotFoundException(String jobId) {
        super(HttpStatus.NOT_FOUND, "Import job not found for id: " + jobId);
    }
}
