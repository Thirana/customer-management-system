package com.example.customermanagement.dto.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustomerResponseDTO {

    private Long id;

    private String name;

    private LocalDate dateOfBirth;

    private String nicNumber;

    private List<String> mobileNumbers = new ArrayList<String>();

    private List<AddressResponseDTO> addresses = new ArrayList<AddressResponseDTO>();

    // Keep this shallow to avoid recursive customer JSON.
    private List<FamilyMemberDTO> familyMembers = new ArrayList<FamilyMemberDTO>();
}
