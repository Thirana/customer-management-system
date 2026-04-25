package com.example.customermanagement.dto.request;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerCreateDTO {

    @NotBlank
    private String name;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String nicNumber;

    private List<String> mobileNumbers = new ArrayList<String>();

    @Valid
    private List<AddressRequestDTO> addresses = new ArrayList<AddressRequestDTO>();

    private List<Long> familyMemberIds = new ArrayList<Long>();
}
