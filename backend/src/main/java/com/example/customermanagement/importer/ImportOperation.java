package com.example.customermanagement.importer;

public enum ImportOperation {
    AUTO,
    CREATE,
    UPDATE;

    public static ImportOperation fromCellValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return AUTO;
        }

        String normalized = value.trim().toUpperCase();
        if ("CREATE".equals(normalized)) {
            return CREATE;
        }
        if ("UPDATE".equals(normalized)) {
            return UPDATE;
        }

        throw new IllegalArgumentException("Operation must be CREATE or UPDATE when provided.");
    }
}
