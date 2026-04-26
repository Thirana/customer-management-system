package com.example.customermanagement.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.customermanagement.constant.ApiConstants;
import com.example.customermanagement.config.AppProperties;
import com.example.customermanagement.dto.request.CustomerCreateDTO;
import com.example.customermanagement.dto.request.CustomerUpdateDTO;
import com.example.customermanagement.dto.response.CityResponseDTO;
import com.example.customermanagement.dto.response.CustomerResponseDTO;
import com.example.customermanagement.dto.response.CustomerSummaryDTO;
import com.example.customermanagement.dto.response.PageResponse;
import com.example.customermanagement.exception.CustomerNotFoundException;
import com.example.customermanagement.exception.InvalidRequestException;
import com.example.customermanagement.service.CustomerService;
import com.example.customermanagement.service.MasterDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {CustomerController.class, MasterDataController.class})
@EnableConfigurationProperties(AppProperties.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private MasterDataService masterDataService;

    @Test
    void listCustomersReturnsEnvelopeAndPassesPaginationParameters() throws Exception {
        PageResponse<CustomerSummaryDTO> page = PageResponse.from(new PageImpl<CustomerSummaryDTO>(
                Collections.singletonList(new CustomerSummaryDTO(
                        1L,
                        "Nimal Perera",
                        LocalDate.of(1990, 1, 1),
                        "NIC-001",
                        1,
                        1
                ))
        ));
        when(customerService.listCustomers(1, 5, "nicNumber", "desc", null)).thenReturn(page);

        mockMvc.perform(get(ApiConstants.API_V1 + "/customers")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "nicNumber")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(header().exists(ApiConstants.REQUEST_ID_HEADER))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].nicNumber").value("NIC-001"))
                .andExpect(jsonPath("$.data.page").value(0));

        verify(customerService).listCustomers(1, 5, "nicNumber", "desc", null);
    }

    @Test
    void listCustomersPassesOptionalSearchParameter() throws Exception {
        PageResponse<CustomerSummaryDTO> page = PageResponse.from(new PageImpl<CustomerSummaryDTO>(
                Collections.singletonList(new CustomerSummaryDTO(
                        2L,
                        "Kasun Silva",
                        LocalDate.of(1991, 2, 2),
                        "NIC-SEARCH-001",
                        0,
                        0
                ))
        ));
        when(customerService.listCustomers(0, 10, "name", "asc", "Kasun")).thenReturn(page);

        mockMvc.perform(get(ApiConstants.API_V1 + "/customers")
                        .param("search", "Kasun"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Kasun Silva"));

        verify(customerService).listCustomers(0, 10, "name", "asc", "Kasun");
    }

    @Test
    void getCustomerReturnsDetailEnvelope() throws Exception {
        when(customerService.getCustomer(1L)).thenReturn(response(1L));

        mockMvc.perform(get(ApiConstants.API_V1 + "/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getCustomerReturnsNotFoundEnvelope() throws Exception {
        when(customerService.getCustomer(99L)).thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(get(ApiConstants.API_V1 + "/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Customer not found for id: 99"));
    }

    @Test
    void createCustomerValidatesRequestBody() throws Exception {
        CustomerCreateDTO request = new CustomerCreateDTO();
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setNicNumber("NIC-002");

        mockMvc.perform(post(ApiConstants.API_V1 + "/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.name").exists());
    }

    @Test
    void createCustomerReturnsCreatedStatus() throws Exception {
        CustomerCreateDTO request = validCreateRequest("NIC-003");
        when(customerService.createCustomer(org.mockito.ArgumentMatchers.any(CustomerCreateDTO.class)))
                .thenReturn(response(3L));

        mockMvc.perform(post(ApiConstants.API_V1 + "/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(3));
    }

    @Test
    void createCustomerReturnsBadRequestForInvalidMobileNumber() throws Exception {
        CustomerCreateDTO request = validCreateRequest("NIC-003-A");
        request.setMobileNumbers(Collections.singletonList("1234"));
        when(customerService.createCustomer(org.mockito.ArgumentMatchers.any(CustomerCreateDTO.class)))
                .thenThrow(new InvalidRequestException("Mobile numbers must contain exactly 10 digits."));

        mockMvc.perform(post(ApiConstants.API_V1 + "/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Mobile numbers must contain exactly 10 digits."));
    }

    @Test
    void updateCustomerReturnsUpdatedDetail() throws Exception {
        CustomerCreateDTO request = validCreateRequest("NIC-004");
        when(customerService.updateCustomer(eq(4L), org.mockito.ArgumentMatchers.any(CustomerUpdateDTO.class)))
                .thenReturn(response(4L));

        mockMvc.perform(put(ApiConstants.API_V1 + "/customers/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(4));
    }

    @Test
    void deleteCustomerReturnsSuccessEnvelope() throws Exception {
        mockMvc.perform(delete(ApiConstants.API_V1 + "/customers/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Customer deleted successfully."));

        verify(customerService).deleteCustomer(5L);
    }

    @Test
    void listCitiesReturnsDropdownShape() throws Exception {
        when(masterDataService.listCities()).thenReturn(Collections.singletonList(
                new CityResponseDTO(10L, "Colombo", 1L, "Sri Lanka")
        ));

        mockMvc.perform(get(ApiConstants.API_V1 + "/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].countryName").value("Sri Lanka"));
    }

    private CustomerCreateDTO validCreateRequest(String nicNumber) {
        CustomerCreateDTO request = new CustomerCreateDTO();
        request.setName("Nimal Perera");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setNicNumber(nicNumber);
        return request;
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
