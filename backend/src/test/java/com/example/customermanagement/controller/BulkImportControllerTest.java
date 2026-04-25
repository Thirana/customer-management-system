package com.example.customermanagement.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.customermanagement.config.AppProperties;
import com.example.customermanagement.constant.ApiConstants;
import com.example.customermanagement.dto.response.ImportStatusDTO;
import com.example.customermanagement.exception.ImportJobNotFoundException;
import com.example.customermanagement.service.BulkImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BulkImportController.class)
@EnableConfigurationProperties(AppProperties.class)
class BulkImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BulkImportService bulkImportService;

    @Test
    void importCustomersReturnsAcceptedStatus() throws Exception {
        ImportStatusDTO status = new ImportStatusDTO();
        status.setJobId("job-123");
        status.setStatus("PROCESSING");
        when(bulkImportService.startImport(org.mockito.ArgumentMatchers.any())).thenReturn(status);

        mockMvc.perform(multipart(ApiConstants.API_V1 + "/customers/import")
                        .file(new MockMultipartFile(
                                "file",
                                "customers.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "content".getBytes()
                        )))
                .andExpect(status().isAccepted())
                .andExpect(header().exists(ApiConstants.REQUEST_ID_HEADER))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").value("job-123"))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
    }

    @Test
    void getImportStatusReturnsTrackedJob() throws Exception {
        ImportStatusDTO status = new ImportStatusDTO();
        status.setJobId("job-999");
        status.setStatus("COMPLETED");
        status.setProcessedCount(10);
        status.setSuccessCount(8);
        status.setFailureCount(2);
        when(bulkImportService.getStatus("job-999")).thenReturn(status);

        mockMvc.perform(get(ApiConstants.API_V1 + "/customers/import/job-999/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jobId").value("job-999"))
                .andExpect(jsonPath("$.data.processedCount").value(10));
    }

    @Test
    void getImportStatusReturnsNotFoundForUnknownJob() throws Exception {
        when(bulkImportService.getStatus("missing-job")).thenThrow(new ImportJobNotFoundException("missing-job"));

        mockMvc.perform(get(ApiConstants.API_V1 + "/customers/import/missing-job/status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Import job not found for id: missing-job"));
    }
}
