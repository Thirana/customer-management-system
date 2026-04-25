package com.example.customermanagement.service;

import com.example.customermanagement.config.AppProperties;
import com.example.customermanagement.dto.response.ImportStatusDTO;
import com.example.customermanagement.exception.InvalidFileException;
import com.example.customermanagement.importer.BulkImportProcessor;
import com.example.customermanagement.importer.ImportProgressTracker;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BulkImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BulkImportService.class);

    private final AppProperties appProperties;
    private final ImportProgressTracker progressTracker;
    private final BulkImportProcessor bulkImportProcessor;

    public BulkImportService(
            AppProperties appProperties,
            ImportProgressTracker progressTracker,
            BulkImportProcessor bulkImportProcessor
    ) {
        this.appProperties = appProperties;
        this.progressTracker = progressTracker;
        this.bulkImportProcessor = bulkImportProcessor;
    }

    public ImportStatusDTO startImport(MultipartFile file) {
        validateFile(file);

        String jobId = UUID.randomUUID().toString();
        Path stagedFile = stageFile(file, jobId);
        ImportStatusDTO status = progressTracker.createJob(jobId);
        String requestId = MDC.get("requestId");

        LOGGER.info("Customer import accepted jobId={} originalFilename={}", jobId, file.getOriginalFilename());
        bulkImportProcessor.processImportAsync(jobId, stagedFile, requestId);
        return status;
    }

    public ImportStatusDTO getStatus(String jobId) {
        return progressTracker.getStatus(jobId);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file is empty.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new InvalidFileException("Uploaded file name is missing.");
        }

        if (!originalFilename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw new InvalidFileException("Only .xlsx files are supported for bulk import.");
        }

        long maxBytes = appProperties.getImport().getMaxFileSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new InvalidFileException("Uploaded file exceeds the allowed size.");
        }
    }

    private Path stageFile(MultipartFile file, String jobId) {
        try {
            // Stage uploads to disk so async processing can stream them after the request thread finishes.
            Path stagedFile = Files.createTempFile("customer-import-" + jobId + "-", ".xlsx");
            file.transferTo(stagedFile.toFile());
            return stagedFile;
        } catch (IOException exception) {
            throw new InvalidFileException("Failed to stage the uploaded file for processing.");
        }
    }
}
