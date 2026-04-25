package com.example.customermanagement.dto.request;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddressRequestDTO {

    private String addressLine1;

    private String addressLine2;

    @NotNull
    private Long cityId;
}
