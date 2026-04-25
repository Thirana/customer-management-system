package com.example.customermanagement.dto.response;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class CustomerSummaryDTO {

    private final Long id;

    private final String name;

    private final LocalDate dateOfBirth;

    private final String nicNumber;

    private final long mobileNumberCount;

    private final long addressCount;

    public CustomerSummaryDTO(
            Long id,
            String name,
            LocalDate dateOfBirth,
            String nicNumber,
            long mobileNumberCount,
            long addressCount
    ) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.nicNumber = nicNumber;
        this.mobileNumberCount = mobileNumberCount;
        this.addressCount = addressCount;
    }
}
