package com.example.customermanagement.controller;

import com.example.customermanagement.constant.ApiConstants;
import com.example.customermanagement.dto.request.CustomerCreateDTO;
import com.example.customermanagement.dto.request.CustomerUpdateDTO;
import com.example.customermanagement.dto.response.ApiResponse;
import com.example.customermanagement.dto.response.CustomerResponseDTO;
import com.example.customermanagement.dto.response.CustomerSummaryDTO;
import com.example.customermanagement.dto.response.PageResponse;
import com.example.customermanagement.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "List customers", description = "Returns a paginated customer summary list.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customers returned")
    @GetMapping
    public ApiResponse<PageResponse<CustomerSummaryDTO>> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ApiResponse.success(
                customerService.listCustomers(page, size, sortBy, sortDir),
                "Customers retrieved successfully."
        );
    }

    @Operation(summary = "Get customer detail", description = "Returns a customer with contact, address, and family data.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer returned")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    @GetMapping("/{id}")
    public ApiResponse<CustomerResponseDTO> getCustomer(@PathVariable Long id) {
        return ApiResponse.success(customerService.getCustomer(id), "Customer retrieved successfully.");
    }

    @Operation(summary = "Create customer", description = "Creates a customer and returns the created detail record.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Customer created")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate NIC")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerCreateDTO request) {
        return ApiResponse.success(customerService.createCustomer(request), "Customer created successfully.");
    }

    @Operation(summary = "Update customer", description = "Updates a customer and returns the updated detail record.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer updated")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate NIC")
    @PutMapping("/{id}")
    public ApiResponse<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateDTO request
    ) {
        return ApiResponse.success(customerService.updateCustomer(id, request), "Customer updated successfully.");
    }

    @Operation(summary = "Delete customer", description = "Deletes a customer and related child records.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Customer deleted")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Customer not found")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ApiResponse.success(null, "Customer deleted successfully.");
    }
}
