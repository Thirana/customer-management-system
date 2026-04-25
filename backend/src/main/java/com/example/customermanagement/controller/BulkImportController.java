package com.example.customermanagement.controller;

import com.example.customermanagement.constant.ApiConstants;
import com.example.customermanagement.dto.response.ApiResponse;
import com.example.customermanagement.dto.response.ImportStatusDTO;
import com.example.customermanagement.service.BulkImportService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/customers/import")
public class BulkImportController {

    private final BulkImportService bulkImportService;

    public BulkImportController(BulkImportService bulkImportService) {
        this.bulkImportService = bulkImportService;
    }

    @Operation(
            summary = "Start a bulk customer import",
            description = "Accepts an .xlsx file, starts async processing, and returns a job ID for polling."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Import accepted")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file")
    @PostMapping
    public ResponseEntity<ApiResponse<ImportStatusDTO>> importCustomers(@RequestParam("file") MultipartFile file) {
        ImportStatusDTO status = bulkImportService.startImport(file);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(status, "Import accepted. Poll the status endpoint for progress."));
    }

    @Operation(
            summary = "Get bulk import status",
            description = "Returns the current import progress, counts, and capped row-level errors."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Import status returned")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Import job not found")
    @GetMapping("/{jobId}/status")
    public ApiResponse<ImportStatusDTO> getImportStatus(@PathVariable String jobId) {
        return ApiResponse.success(bulkImportService.getStatus(jobId), "Import status retrieved successfully.");
    }
}
