package com.example.customermanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseDTO {

    private Long id;

    private String addressLine1;

    private String addressLine2;

    private Long cityId;

    private String cityName;

    private Long countryId;

    private String countryName;
}
