package com.example.customermanagement.controller;

import com.example.customermanagement.constant.ApiConstants;
import com.example.customermanagement.dto.response.ApiResponse;
import com.example.customermanagement.dto.response.CityResponseDTO;
import com.example.customermanagement.service.MasterDataService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.API_V1)
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Operation(summary = "List cities", description = "Returns city and country values for customer address dropdowns.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cities returned")
    @GetMapping("/cities")
    public ApiResponse<List<CityResponseDTO>> listCities() {
        return ApiResponse.success(masterDataService.listCities(), "Cities retrieved successfully.");
    }
}
