package com.example.customermanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CityResponseDTO {

    private Long id;

    private String name;

    private Long countryId;

    private String countryName;
}
