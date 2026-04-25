package com.example.customermanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.customermanagement.config.AppProperties;
import com.example.customermanagement.dto.response.ImportStatusDTO;
import com.example.customermanagement.exception.InvalidFileException;
import com.example.customermanagement.importer.BulkImportProcessor;
import com.example.customermanagement.importer.ImportProgressTracker;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class BulkImportServiceTest {

    @Mock
    private ImportProgressTracker progressTracker;

    @Mock
    private BulkImportProcessor bulkImportProcessor;

    private Path stagedPathToDelete;

    @AfterEach
    void tearDown() throws Exception {
        MDC.clear();
        if (stagedPathToDelete != null) {
            Files.deleteIfExists(stagedPathToDelete);
        }
    }

    @Test
    void startImportRejectsEmptyFile() {
        BulkImportService bulkImportService = new BulkImportService(appProperties(), progressTracker, bulkImportProcessor);
        MultipartFile file = new MockMultipartFile("file", "customers.xlsx", "application/octet-stream", new byte[0]);

        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> bulkImportService.startImport(file));

        assertEquals("Uploaded file is empty.", exception.getMessage());
    }

    @Test
    void startImportRejectsWrongExtension() {
        BulkImportService bulkImportService = new BulkImportService(appProperties(), progressTracker, bulkImportProcessor);
        MultipartFile file = new MockMultipartFile("file", "customers.csv", "text/csv", "content".getBytes());

        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> bulkImportService.startImport(file));

        assertEquals("Only .xlsx files are supported for bulk import.", exception.getMessage());
    }

    @Test
    void startImportStagesFileCreatesJobAndDelegatesToAsyncProcessor() throws Exception {
        BulkImportService bulkImportService = new BulkImportService(appProperties(), progressTracker, bulkImportProcessor);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "customers.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "dummy workbook bytes".getBytes()
        );
        ImportStatusDTO initialStatus = new ImportStatusDTO();
        initialStatus.setJobId("generated-job-id");
        initialStatus.setStatus("PROCESSING");
        when(progressTracker.createJob(any(String.class))).thenReturn(initialStatus);
        MDC.put("requestId", "request-123");

        ImportStatusDTO result = bulkImportService.startImport(file);

        ArgumentCaptor<String> jobIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        verify(progressTracker).createJob(jobIdCaptor.capture());
        verify(bulkImportProcessor).processImportAsync(eq(jobIdCaptor.getValue()), pathCaptor.capture(), eq("request-123"));

        stagedPathToDelete = pathCaptor.getValue();
        assertTrue(Files.exists(stagedPathToDelete));
        assertEquals(initialStatus, result);
    }

    private AppProperties appProperties() {
        AppProperties appProperties = new AppProperties();
        appProperties.getImport().setMaxFileSizeMb(10);
        appProperties.getImport().setMaxStoredErrors(100);
        appProperties.getImport().setBatchSize(500);
        return appProperties;
    }
}
