package com.example.customermanagement.importer;

import java.time.LocalDate;

public class ImportRowCommand {

    private final int rowNumber;
    private final String name;
    private final LocalDate dateOfBirth;
    private final String nicNumber;
    private final ImportOperation operation;

    public ImportRowCommand(int rowNumber, String name, LocalDate dateOfBirth, String nicNumber, ImportOperation operation) {
        this.rowNumber = rowNumber;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.nicNumber = nicNumber;
        this.operation = operation;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getNicNumber() {
        return nicNumber;
    }

    public ImportOperation getOperation() {
        return operation;
    }
}
