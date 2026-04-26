package com.example.customermanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.customermanagement.dto.request.AddressRequestDTO;
import com.example.customermanagement.dto.request.CustomerCreateDTO;
import com.example.customermanagement.dto.request.CustomerUpdateDTO;
import com.example.customermanagement.dto.response.CustomerResponseDTO;
import com.example.customermanagement.entity.City;
import com.example.customermanagement.entity.Country;
import com.example.customermanagement.entity.Customer;
import com.example.customermanagement.exception.CustomerNotFoundException;
import com.example.customermanagement.exception.DuplicateNicException;
import com.example.customermanagement.exception.InvalidRequestException;
import com.example.customermanagement.exception.MasterDataNotFoundException;
import com.example.customermanagement.mapper.CustomerMapper;
import com.example.customermanagement.repository.CityRepository;
import com.example.customermanagement.repository.CustomerRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CustomerMapper customerMapper;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, cityRepository, customerMapper);
    }

    @Test
    void createCustomerSavesValidRequest() {
        CustomerCreateDTO request = createRequest("NIC-001");
        CustomerResponseDTO response = response(1L);
        when(customerRepository.existsByNicNumber("NIC-001")).thenReturn(false);
        when(cityRepository.findById(10L)).thenReturn(Optional.of(city(10L)));
        when(customerRepository.findAllById(Collections.singleton(2L)))
                .thenReturn(Collections.singletonList(customer(2L, "NIC-FAMILY")));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(customerMapper.toCustomerResponse(any(Customer.class))).thenReturn(response);

        CustomerResponseDTO result = customerService.createCustomer(request);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertEquals("Nimal Perera", savedCustomer.getName());
        assertEquals("NIC-001", savedCustomer.getNicNumber());
        assertEquals(1, savedCustomer.getMobileNumbers().size());
        assertEquals(1, savedCustomer.getAddresses().size());
        assertEquals(1, savedCustomer.getFamilyMembers().size());
        assertEquals(response, result);
    }

    @Test
    void createCustomerRejectsDuplicateNic() {
        CustomerCreateDTO request = createRequest("NIC-002");
        when(customerRepository.existsByNicNumber("NIC-002")).thenReturn(true);

        assertThrows(DuplicateNicException.class, () -> customerService.createCustomer(request));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void createCustomerRejectsInvalidCityId() {
        CustomerCreateDTO request = createRequest("NIC-003");
        when(customerRepository.existsByNicNumber("NIC-003")).thenReturn(false);
        when(cityRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(MasterDataNotFoundException.class, () -> customerService.createCustomer(request));
    }

    @Test
    void createCustomerRejectsInvalidFamilyMemberId() {
        CustomerCreateDTO request = createRequest("NIC-004");
        when(customerRepository.existsByNicNumber("NIC-004")).thenReturn(false);
        when(cityRepository.findById(10L)).thenReturn(Optional.of(city(10L)));
        when(customerRepository.findAllById(Collections.singleton(2L))).thenReturn(Collections.<Customer>emptyList());

        assertThrows(CustomerNotFoundException.class, () -> customerService.createCustomer(request));
    }

    @Test
    void createCustomerRejectsInvalidMobileNumber() {
        CustomerCreateDTO request = createRequest("NIC-004-A");
        request.setMobileNumbers(Collections.singletonList("07712ABC"));
        when(customerRepository.existsByNicNumber("NIC-004-A")).thenReturn(false);

        assertThrows(InvalidRequestException.class, () -> customerService.createCustomer(request));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomerReplacesMutableFields() {
        CustomerUpdateDTO request = updateRequest("NIC-005-UPDATED");
        Customer existingCustomer = customer(5L, "NIC-005");
        CustomerResponseDTO response = response(5L);
        when(customerRepository.findById(5L)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByNicNumberAndIdNot("NIC-005-UPDATED", 5L)).thenReturn(false);
        when(cityRepository.findById(10L)).thenReturn(Optional.of(city(10L)));
        when(customerRepository.findAllById(Collections.singleton(2L)))
                .thenReturn(Collections.singletonList(customer(2L, "NIC-FAMILY")));
        when(customerMapper.toCustomerResponse(existingCustomer)).thenReturn(response);

        CustomerResponseDTO result = customerService.updateCustomer(5L, request);

        assertEquals("Kamal Silva", existingCustomer.getName());
        assertEquals("NIC-005-UPDATED", existingCustomer.getNicNumber());
        assertEquals(1, existingCustomer.getMobileNumbers().size());
        assertEquals(1, existingCustomer.getAddresses().size());
        assertEquals(response, result);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomerRejectsDuplicateNic() {
        CustomerUpdateDTO request = updateRequest("NIC-006");
        when(customerRepository.findById(6L)).thenReturn(Optional.of(customer(6L, "OLD-NIC")));
        when(customerRepository.existsByNicNumberAndIdNot("NIC-006", 6L)).thenReturn(true);

        assertThrows(DuplicateNicException.class, () -> customerService.updateCustomer(6L, request));
    }

    @Test
    void updateCustomerRejectsSelfFamilyMember() {
        CustomerUpdateDTO request = updateRequest("NIC-007");
        request.setFamilyMemberIds(Collections.singletonList(7L));
        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer(7L, "NIC-007")));
        when(customerRepository.existsByNicNumberAndIdNot("NIC-007", 7L)).thenReturn(false);
        when(cityRepository.findById(10L)).thenReturn(Optional.of(city(10L)));

        assertThrows(InvalidRequestException.class, () -> customerService.updateCustomer(7L, request));
    }

    @Test
    void updateCustomerRejectsInvalidMobileNumber() {
        CustomerUpdateDTO request = updateRequest("NIC-007-A");
        request.setMobileNumbers(Collections.singletonList("1234"));
        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer(7L, "NIC-007-A")));
        when(customerRepository.existsByNicNumberAndIdNot("NIC-007-A", 7L)).thenReturn(false);

        assertThrows(InvalidRequestException.class, () -> customerService.updateCustomer(7L, request));
    }

    @Test
    void getUpdateAndDeleteRejectMissingCustomer() {
        when(customerRepository.findDetailById(8L)).thenReturn(Optional.empty());
        when(customerRepository.findById(8L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomer(8L));
        assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomer(8L, updateRequest("NIC-008")));
        assertThrows(CustomerNotFoundException.class, () -> customerService.deleteCustomer(8L));
    }

    @Test
    void deleteCustomerRemovesExistingCustomer() {
        Customer customer = customer(9L, "NIC-009");
        when(customerRepository.findById(9L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(9L);

        verify(customerRepository).delete(customer);
    }

    @Test
    void listCustomersPassesWhitelistedPaginationToRepository() {
        when(customerRepository.findCustomerSummaries(any())).thenReturn(new org.springframework.data.domain.PageImpl<>(
                Collections.emptyList()
        ));

        customerService.listCustomers(0, 20, "nicNumber", "desc", null);

        verify(customerRepository).findCustomerSummaries(any());
    }

    @Test
    void listCustomersUsesSearchQueryWhenPresent() {
        when(customerRepository.findCustomerSummariesBySearch(eq("Kasun"), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(Collections.emptyList()));

        customerService.listCustomers(0, 20, "name", "asc", "  Kasun  ");

        verify(customerRepository).findCustomerSummariesBySearch(eq("Kasun"), any());
    }

    @Test
    void listCustomersRejectsUnknownSortField() {
        assertThrows(
                InvalidRequestException.class,
                () -> customerService.listCustomers(0, 10, "unknown", "asc", null)
        );
    }

    private CustomerCreateDTO createRequest(String nicNumber) {
        CustomerCreateDTO request = new CustomerCreateDTO();
        request.setName("Nimal Perera");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setNicNumber(nicNumber);
        request.setMobileNumbers(Collections.singletonList("0771234567"));
        request.setAddresses(Collections.singletonList(addressRequest()));
        request.setFamilyMemberIds(Collections.singletonList(2L));
        return request;
    }

    private CustomerUpdateDTO updateRequest(String nicNumber) {
        CustomerUpdateDTO request = new CustomerUpdateDTO();
        request.setName("Kamal Silva");
        request.setDateOfBirth(LocalDate.of(1991, 2, 2));
        request.setNicNumber(nicNumber);
        request.setMobileNumbers(Collections.singletonList("0711234567"));
        request.setAddresses(Collections.singletonList(addressRequest()));
        request.setFamilyMemberIds(Collections.singletonList(2L));
        return request;
    }

    private AddressRequestDTO addressRequest() {
        AddressRequestDTO request = new AddressRequestDTO();
        request.setAddressLine1("Line 1");
        request.setCityId(10L);
        return request;
    }

    private Customer customer(Long id, String nicNumber) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName("Customer " + id);
        customer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customer.setNicNumber(nicNumber);
        return customer;
    }

    private City city(Long id) {
        Country country = new Country();
        country.setId(1L);
        country.setName("Sri Lanka");

        City city = new City();
        city.setId(id);
        city.setName("Colombo");
        city.setCountry(country);
        return city;
    }

    private CustomerResponseDTO response(Long id) {
        CustomerResponseDTO response = new CustomerResponseDTO();
        response.setId(id);
        response.setName("Customer " + id);
        response.setDateOfBirth(LocalDate.of(1990, 1, 1));
        response.setNicNumber("NIC-" + id);
        return response;
    }
}
