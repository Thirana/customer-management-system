package com.example.customermanagement.mapper;

import com.example.customermanagement.dto.response.AddressResponseDTO;
import com.example.customermanagement.dto.response.CityResponseDTO;
import com.example.customermanagement.dto.response.CustomerResponseDTO;
import com.example.customermanagement.dto.response.FamilyMemberDTO;
import com.example.customermanagement.entity.Address;
import com.example.customermanagement.entity.City;
import com.example.customermanagement.entity.Customer;
import com.example.customermanagement.entity.MobileNumber;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponseDTO toCustomerResponse(Customer customer) {
        CustomerResponseDTO response = new CustomerResponseDTO();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setDateOfBirth(customer.getDateOfBirth());
        response.setNicNumber(customer.getNicNumber());
        response.setMobileNumbers(toMobileNumbers(customer));
        response.setAddresses(toAddressResponses(customer));
        response.setFamilyMembers(toFamilyMemberResponses(customer));
        return response;
    }

    public CityResponseDTO toCityResponse(City city) {
        return new CityResponseDTO(
                city.getId(),
                city.getName(),
                city.getCountry().getId(),
                city.getCountry().getName()
        );
    }

    private List<String> toMobileNumbers(Customer customer) {
        List<MobileNumber> mobileNumbers = new ArrayList<MobileNumber>(customer.getMobileNumbers());
        mobileNumbers.sort(Comparator.comparing(MobileNumber::getId, Comparator.nullsLast(Long::compareTo)));

        List<String> response = new ArrayList<String>();
        for (MobileNumber mobileNumber : mobileNumbers) {
            response.add(mobileNumber.getMobileNumber());
        }
        return response;
    }

    private List<AddressResponseDTO> toAddressResponses(Customer customer) {
        List<Address> addresses = new ArrayList<Address>(customer.getAddresses());
        addresses.sort(Comparator.comparing(Address::getId, Comparator.nullsLast(Long::compareTo)));

        List<AddressResponseDTO> response = new ArrayList<AddressResponseDTO>();
        for (Address address : addresses) {
            City city = address.getCity();
            response.add(new AddressResponseDTO(
                    address.getId(),
                    address.getAddressLine1(),
                    address.getAddressLine2(),
                    city.getId(),
                    city.getName(),
                    city.getCountry().getId(),
                    city.getCountry().getName()
            ));
        }
        return response;
    }

    private List<FamilyMemberDTO> toFamilyMemberResponses(Customer customer) {
        List<Customer> familyMembers = new ArrayList<Customer>(customer.getFamilyMembers());
        familyMembers.sort(Comparator.comparing(Customer::getName));

        List<FamilyMemberDTO> response = new ArrayList<FamilyMemberDTO>();
        for (Customer familyMember : familyMembers) {
            response.add(new FamilyMemberDTO(
                    familyMember.getId(),
                    familyMember.getName(),
                    familyMember.getNicNumber()
            ));
        }
        return response;
    }
}
