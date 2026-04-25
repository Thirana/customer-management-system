package com.example.customermanagement.service;

import com.example.customermanagement.dto.response.CityResponseDTO;
import com.example.customermanagement.entity.City;
import com.example.customermanagement.mapper.CustomerMapper;
import com.example.customermanagement.repository.CityRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MasterDataService {

    private final CityRepository cityRepository;
    private final CustomerMapper customerMapper;

    public MasterDataService(CityRepository cityRepository, CustomerMapper customerMapper) {
        this.cityRepository = cityRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional(readOnly = true)
    public List<CityResponseDTO> listCities() {
        List<CityResponseDTO> responses = new ArrayList<CityResponseDTO>();
        for (City city : cityRepository.findAllByOrderByCountryNameAscNameAsc()) {
            responses.add(customerMapper.toCityResponse(city));
        }
        return responses;
    }
}
