package com.example.customermanagement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.customermanagement.constant.ApiConstants;
import com.example.customermanagement.dto.request.AddressRequestDTO;
import com.example.customermanagement.dto.request.CustomerCreateDTO;
import com.example.customermanagement.dto.request.CustomerUpdateDTO;
import com.example.customermanagement.entity.City;
import com.example.customermanagement.entity.Country;
import com.example.customermanagement.repository.CityRepository;
import com.example.customermanagement.repository.CountryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class CustomerApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CityRepository cityRepository;

    @Test
    void customerCrudFlowWorksEndToEnd() throws Exception {
        City city = saveCity("Sri Lanka", "Colombo");

        CustomerCreateDTO createRequest = new CustomerCreateDTO();
        createRequest.setName("Integration Test Customer");
        createRequest.setDateOfBirth(LocalDate.of(1992, 3, 4));
        createRequest.setNicNumber("INT-NIC-001");
        createRequest.setMobileNumbers(Collections.singletonList("0771234567"));
        createRequest.setAddresses(Collections.singletonList(address(city.getId(), "123 Integration Road")));

        MvcResult createResult = mockMvc.perform(post(ApiConstants.API_V1 + "/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(ApiConstants.REQUEST_ID_HEADER))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Integration Test Customer"))
                .andReturn();

        Long customerId = extractCustomerId(createResult);

        mockMvc.perform(get(ApiConstants.API_V1 + "/customers/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nicNumber").value("INT-NIC-001"))
                .andExpect(jsonPath("$.data.addresses[0].cityId").value(city.getId()))
                .andExpect(jsonPath("$.data.addresses[0].cityName").value("Colombo"));

        CustomerUpdateDTO updateRequest = new CustomerUpdateDTO();
        updateRequest.setName("Integration Updated Customer");
        updateRequest.setDateOfBirth(LocalDate.of(1993, 4, 5));
        updateRequest.setNicNumber("INT-NIC-001");
        updateRequest.setMobileNumbers(Collections.singletonList("0717654321"));
        updateRequest.setAddresses(Collections.singletonList(address(city.getId(), "456 Updated Street")));

        mockMvc.perform(put(ApiConstants.API_V1 + "/customers/" + customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Integration Updated Customer"))
                .andExpect(jsonPath("$.data.mobileNumbers[0]").value("0717654321"));

        mockMvc.perform(get(ApiConstants.API_V1 + "/customers")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Integration Updated Customer"))
                .andExpect(jsonPath("$.data.content[0].nicNumber").value("INT-NIC-001"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    private Long extractCustomerId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private AddressRequestDTO address(Long cityId, String line1) {
        AddressRequestDTO request = new AddressRequestDTO();
        request.setAddressLine1(line1);
        request.setCityId(cityId);
        return request;
    }

    private City saveCity(String countryName, String cityName) {
        Country country = new Country();
        country.setName(countryName);
        countryRepository.save(country);

        City city = new City();
        city.setName(cityName);
        city.setCountry(country);
        return cityRepository.save(city);
    }
}
