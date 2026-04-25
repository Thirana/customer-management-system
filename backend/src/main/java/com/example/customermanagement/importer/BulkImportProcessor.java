package com.example.customermanagement.importer;

import com.example.customermanagement.config.AppProperties;
import com.example.customermanagement.entity.Customer;
import com.example.customermanagement.exception.InvalidFileException;
import com.example.customermanagement.repository.CustomerRepository;
import com.github.pjfanning.xlsx.StreamingReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class BulkImportProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BulkImportProcessor.class);
    private static final String HEADER_NAME = "Name";
    private static final String HEADER_DATE_OF_BIRTH = "Date of Birth";
    private static final String HEADER_NIC_NUMBER = "NIC Number";
    private static final String HEADER_OPERATION = "Operation";
    private static final String GENERIC_FAILURE_MESSAGE = "Import processing failed unexpectedly.";

    private final CustomerRepository customerRepository;
    private final ImportProgressTracker progressTracker;
    private final EntityManager entityManager;
    private final AppProperties appProperties;
    private final DataFormatter dataFormatter = new DataFormatter();

    public BulkImportProcessor(
            CustomerRepository customerRepository,
            ImportProgressTracker progressTracker,
            EntityManager entityManager,
            AppProperties appProperties
    ) {
        this.customerRepository = customerRepository;
        this.progressTracker = progressTracker;
        this.entityManager = entityManager;
        this.appProperties = appProperties;
    }

    @Async("importTaskExecutor")
    public void processImportAsync(String jobId, Path stagedFile, String requestId) {
        processImport(jobId, stagedFile, requestId);
    }

    void processImport(String jobId, Path stagedFile, String requestId) {
        if (requestId != null && !requestId.trim().isEmpty()) {
            MDC.put("requestId", requestId);
        }

        try {
            int totalRows = countDataRows(stagedFile);
            progressTracker.setTotalCount(jobId, totalRows);
            processWorkbookRows(jobId, stagedFile);
            progressTracker.markCompleted(jobId);

            LOGGER.info(
                    "Customer import completed jobId={} totalCount={} successCount={} failureCount={}",
                    jobId,
                    progressTracker.getStatus(jobId).getTotalCount(),
                    progressTracker.getStatus(jobId).getSuccessCount(),
                    progressTracker.getStatus(jobId).getFailureCount()
            );
        } catch (InvalidFileException exception) {
            LOGGER.warn("Customer import failed validation jobId={} reason={}", jobId, exception.getMessage());
            progressTracker.markFailed(jobId, exception.getMessage(), appProperties.getImport().getMaxStoredErrors());
        } catch (Exception exception) {
            LOGGER.error("Customer import failed unexpectedly jobId={}", jobId, exception);
            progressTracker.markFailed(jobId, GENERIC_FAILURE_MESSAGE, appProperties.getImport().getMaxStoredErrors());
        } finally {
            tryDeleteStagedFile(stagedFile);
            MDC.clear();
        }
    }

    private int countDataRows(Path stagedFile) {
        int totalRows = 0;
        try (InputStream inputStream = Files.newInputStream(stagedFile);
             Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(inputStream)) {
            Sheet sheet = getFirstSheet(workbook);
            Iterator<Row> rows = sheet.iterator();
            validateHeader(rows);

            while (rows.hasNext()) {
                Row row = rows.next();
                if (!isBlankRow(row)) {
                    totalRows++;
                }
            }
        } catch (IOException exception) {
            throw new InvalidFileException("Failed to read the uploaded Excel file.");
        }
        return totalRows;
    }

    private void processWorkbookRows(String jobId, Path stagedFile) {
        int batchSize = appProperties.getImport().getBatchSize();
        List<ImportRowCommand> batch = new ArrayList<ImportRowCommand>(batchSize);
        Set<String> batchNicNumbers = new HashSet<String>();

        try (InputStream inputStream = Files.newInputStream(stagedFile);
             Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(inputStream)) {
            Sheet sheet = getFirstSheet(workbook);
            Iterator<Row> rows = sheet.iterator();
            validateHeader(rows);

            while (rows.hasNext()) {
                Row row = rows.next();
                if (isBlankRow(row)) {
                    continue;
                }

                int rowNumber = row.getRowNum() + 1;
                try {
                    ImportRowCommand command = parseRow(row);
                    if (!batchNicNumbers.add(command.getNicNumber())) {
                        progressTracker.recordRowFailure(
                                jobId,
                                rowNumber,
                                "Duplicate NIC Number found within the same import batch.",
                                appProperties.getImport().getMaxStoredErrors()
                        );
                        continue;
                    }

                    batch.add(command);
                    if (batch.size() >= batchSize) {
                        processBatch(jobId, batch);
                        batch.clear();
                        batchNicNumbers.clear();
                    }
                } catch (RowValidationException exception) {
                    progressTracker.recordRowFailure(
                            jobId,
                            rowNumber,
                            exception.getMessage(),
                            appProperties.getImport().getMaxStoredErrors()
                    );
                }
            }

            if (!batch.isEmpty()) {
                processBatch(jobId, batch);
            }
        } catch (IOException exception) {
            throw new InvalidFileException("Failed to read the uploaded Excel file.");
        }
    }

    private void processBatch(String jobId, List<ImportRowCommand> batch) {
        List<String> nicNumbers = new ArrayList<String>(batch.size());
        for (ImportRowCommand command : batch) {
            nicNumbers.add(command.getNicNumber());
        }

        Map<String, Customer> existingCustomersByNic = new LinkedHashMap<String, Customer>();
        List<Customer> existingCustomers = customerRepository.findByNicNumberIn(nicNumbers);
        for (Customer existingCustomer : existingCustomers) {
            existingCustomersByNic.put(existingCustomer.getNicNumber(), existingCustomer);
        }

        List<Customer> customersToSave = new ArrayList<Customer>();
        int successCount = 0;

        for (ImportRowCommand command : batch) {
            Customer existingCustomer = existingCustomersByNic.get(command.getNicNumber());
            ImportOperation resolvedOperation = resolveOperation(command.getOperation(), existingCustomer != null);

            if (resolvedOperation == ImportOperation.CREATE && existingCustomer != null) {
                progressTracker.recordRowFailure(
                        jobId,
                        command.getRowNumber(),
                        "Customer already exists for NIC Number: " + command.getNicNumber(),
                        appProperties.getImport().getMaxStoredErrors()
                );
                continue;
            }

            if (resolvedOperation == ImportOperation.UPDATE && existingCustomer == null) {
                progressTracker.recordRowFailure(
                        jobId,
                        command.getRowNumber(),
                        "Customer not found for NIC Number: " + command.getNicNumber(),
                        appProperties.getImport().getMaxStoredErrors()
                );
                continue;
            }

            Customer customer = existingCustomer != null ? existingCustomer : new Customer();
            // Import rows only manage the core customer columns defined by the Excel contract.
            customer.setName(command.getName());
            customer.setDateOfBirth(command.getDateOfBirth());
            customer.setNicNumber(command.getNicNumber());
            customersToSave.add(customer);
            successCount++;
        }

        if (!customersToSave.isEmpty()) {
            customerRepository.saveAll(customersToSave);
            customerRepository.flush();
            // Clear the persistence context between batches so large imports do not keep growing memory.
            entityManager.clear();
            progressTracker.recordSuccesses(jobId, successCount);
        }
    }

    private Sheet getFirstSheet(Workbook workbook) {
        if (workbook.getNumberOfSheets() < 1) {
            throw new InvalidFileException("Uploaded Excel file does not contain any sheets.");
        }
        return workbook.getSheetAt(0);
    }

    private void validateHeader(Iterator<Row> rows) {
        if (!rows.hasNext()) {
            throw new InvalidFileException("Uploaded Excel file does not contain a header row.");
        }

        Row headerRow = rows.next();
        validateHeaderCell(headerRow, 0, HEADER_NAME);
        validateHeaderCell(headerRow, 1, HEADER_DATE_OF_BIRTH);
        validateHeaderCell(headerRow, 2, HEADER_NIC_NUMBER);

        String operationHeader = readCellAsString(headerRow.getCell(3));
        if (operationHeader != null
                && !operationHeader.trim().isEmpty()
                && !HEADER_OPERATION.equalsIgnoreCase(operationHeader.trim())) {
            throw new InvalidFileException("Column D must be named Operation when present.");
        }
    }

    private void validateHeaderCell(Row headerRow, int cellIndex, String expectedValue) {
        String actualValue = readCellAsString(headerRow.getCell(cellIndex));
        if (actualValue == null || !expectedValue.equalsIgnoreCase(actualValue.trim())) {
            throw new InvalidFileException("Missing required column: " + expectedValue);
        }
    }

    private ImportRowCommand parseRow(Row row) {
        String name = normalizeString(readCellAsString(row.getCell(0)));
        if (name == null) {
            throw new RowValidationException("Name is required.");
        }

        LocalDate dateOfBirth = readDate(row.getCell(1));
        if (dateOfBirth == null) {
            throw new RowValidationException("Date of Birth is required.");
        }

        String nicNumber = normalizeString(readCellAsString(row.getCell(2)));
        if (nicNumber == null) {
            throw new RowValidationException("NIC Number is required.");
        }

        try {
            ImportOperation operation = ImportOperation.fromCellValue(readCellAsString(row.getCell(3)));
            return new ImportRowCommand(row.getRowNum() + 1, name, dateOfBirth, nicNumber, operation);
        } catch (IllegalArgumentException exception) {
            throw new RowValidationException(exception.getMessage());
        }
    }

    private LocalDate readDate(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            if (!DateUtil.isCellDateFormatted(cell)) {
                throw new RowValidationException("Date of Birth must be an Excel date or yyyy-MM-dd string.");
            }
            Date date = cell.getDateCellValue();
            return Instant.ofEpochMilli(date.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        String value = normalizeString(readCellAsString(cell));
        if (value == null) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new RowValidationException("Date of Birth must be in yyyy-MM-dd format.");
        }
    }

    private String readCellAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        return dataFormatter.formatCellValue(cell);
    }

    private String normalizeString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private ImportOperation resolveOperation(ImportOperation requestedOperation, boolean customerExists) {
        if (requestedOperation == ImportOperation.AUTO) {
            return customerExists ? ImportOperation.UPDATE : ImportOperation.CREATE;
        }
        return requestedOperation;
    }

    private boolean isBlankRow(Row row) {
        for (int index = 0; index < 4; index++) {
            String value = normalizeString(readCellAsString(row.getCell(index)));
            if (value != null) {
                return false;
            }
        }
        return true;
    }

    private void tryDeleteStagedFile(Path stagedFile) {
        try {
            Files.deleteIfExists(stagedFile);
        } catch (IOException exception) {
            LOGGER.warn("Failed to delete staged import file path={}", stagedFile);
        }
    }

    private static final class RowValidationException extends RuntimeException {

        private RowValidationException(String message) {
            super(message);
        }
    }
}
