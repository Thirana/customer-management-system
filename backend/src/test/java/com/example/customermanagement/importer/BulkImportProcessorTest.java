package com.example.customermanagement.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.customermanagement.config.AppProperties;
import com.example.customermanagement.dto.response.ImportStatusDTO;
import com.example.customermanagement.entity.Customer;
import com.example.customermanagement.repository.CustomerRepository;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BulkImportProcessorTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EntityManager entityManager;

    @Test
    @SuppressWarnings("unchecked")
    void processImportHandlesCreateUpdateAndRowValidationErrors() throws Exception {
        ImportProgressTracker progressTracker = new ImportProgressTracker();
        AppProperties appProperties = appProperties();
        BulkImportProcessor processor = new BulkImportProcessor(customerRepository, progressTracker, entityManager, appProperties);
        Customer existingCustomer = customer(10L, "NIC-EXISTING", "Old Name", LocalDate.of(1980, 1, 1));
        when(customerRepository.findByNicNumberIn(any())).thenReturn(Collections.singletonList(existingCustomer));
        when(customerRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String jobId = "job-create-update";
        progressTracker.createJob(jobId);
        Path workbook = createWorkbook(
                row("Name", "Date of Birth", "NIC Number", "Operation"),
                row("New Customer", "1990-01-01", "NIC-NEW", "CREATE"),
                row("Updated Existing", LocalDate.of(1991, 2, 2), "NIC-EXISTING", "UPDATE"),
                row("", "1992-03-03", "NIC-MISSING-NAME", "CREATE"),
                row("Bad Operation", "1993-04-04", "NIC-BAD-OP", "UPSERT")
        );

        processor.processImport(jobId, workbook, "request-1");

        ArgumentCaptor<List<Customer>> savedCustomersCaptor = ArgumentCaptor.forClass(List.class);
        verify(customerRepository).saveAll(savedCustomersCaptor.capture());
        verify(customerRepository).flush();
        verify(entityManager).clear();
        assertFalse(Files.exists(workbook));

        List<Customer> savedCustomers = savedCustomersCaptor.getValue();
        assertEquals(2, savedCustomers.size());
        assertEquals("NIC-NEW", savedCustomers.get(0).getNicNumber());
        assertEquals("New Customer", savedCustomers.get(0).getName());
        assertEquals("Updated Existing", existingCustomer.getName());
        assertEquals(LocalDate.of(1991, 2, 2), existingCustomer.getDateOfBirth());

        ImportStatusDTO status = progressTracker.getStatus(jobId);
        assertEquals("COMPLETED", status.getStatus());
        assertEquals(Integer.valueOf(4), status.getTotalCount());
        assertEquals(4, status.getProcessedCount());
        assertEquals(2, status.getSuccessCount());
        assertEquals(2, status.getFailureCount());
        assertEquals(100, status.getProgressPercent());
        assertEquals(2, status.getErrors().size());
        assertEquals(4, status.getErrors().get(0).getRowNumber());
        assertEquals("Name is required.", status.getErrors().get(0).getMessage());
        assertEquals(5, status.getErrors().get(1).getRowNumber());
    }

    @Test
    void processImportRejectsCreateForExistingCustomerAndUpdateForMissingCustomer() throws Exception {
        ImportProgressTracker progressTracker = new ImportProgressTracker();
        AppProperties appProperties = appProperties();
        BulkImportProcessor processor = new BulkImportProcessor(customerRepository, progressTracker, entityManager, appProperties);
        when(customerRepository.findByNicNumberIn(any()))
                .thenReturn(Collections.singletonList(customer(20L, "NIC-EXISTING", "Existing", LocalDate.of(1990, 1, 1))));

        String jobId = "job-create-update-errors";
        progressTracker.createJob(jobId);
        Path workbook = createWorkbook(
                row("Name", "Date of Birth", "NIC Number", "Operation"),
                row("Already Exists", "1990-01-01", "NIC-EXISTING", "CREATE"),
                row("Missing Customer", "1991-02-02", "NIC-MISSING", "UPDATE")
        );

        processor.processImport(jobId, workbook, "request-2");

        verify(customerRepository, never()).saveAll(any());
        assertFalse(Files.exists(workbook));

        ImportStatusDTO status = progressTracker.getStatus(jobId);
        assertEquals("COMPLETED", status.getStatus());
        assertEquals(2, status.getProcessedCount());
        assertEquals(0, status.getSuccessCount());
        assertEquals(2, status.getFailureCount());
        assertTrue(status.getErrors().get(0).getMessage().contains("Customer already exists"));
        assertTrue(status.getErrors().get(1).getMessage().contains("Customer not found"));
    }

    @Test
    void processImportRejectsDuplicateNicWithinSameBatch() throws Exception {
        ImportProgressTracker progressTracker = new ImportProgressTracker();
        AppProperties appProperties = appProperties();
        appProperties.getImport().setBatchSize(10);
        BulkImportProcessor processor = new BulkImportProcessor(customerRepository, progressTracker, entityManager, appProperties);
        when(customerRepository.findByNicNumberIn(any())).thenReturn(Collections.<Customer>emptyList());
        when(customerRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String jobId = "job-duplicate-batch";
        progressTracker.createJob(jobId);
        Path workbook = createWorkbook(
                row("Name", "Date of Birth", "NIC Number", "Operation"),
                row("First Row", "1990-01-01", "NIC-DUPLICATE", "CREATE"),
                row("Second Row", "1991-02-02", "NIC-DUPLICATE", "CREATE")
        );

        processor.processImport(jobId, workbook, "request-3");

        ImportStatusDTO status = progressTracker.getStatus(jobId);
        assertEquals("COMPLETED", status.getStatus());
        assertEquals(2, status.getProcessedCount());
        assertEquals(1, status.getSuccessCount());
        assertEquals(1, status.getFailureCount());
        assertTrue(status.getErrors().get(0).getMessage().contains("Duplicate NIC Number"));
    }

    private AppProperties appProperties() {
        AppProperties appProperties = new AppProperties();
        appProperties.getImport().setBatchSize(50);
        appProperties.getImport().setMaxStoredErrors(10);
        return appProperties;
    }

    private Customer customer(Long id, String nicNumber, String name, LocalDate dateOfBirth) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setNicNumber(nicNumber);
        customer.setName(name);
        customer.setDateOfBirth(dateOfBirth);
        return customer;
    }

    private Object[] row(Object name, Object dateOfBirth, Object nicNumber, Object operation) {
        return new Object[]{name, dateOfBirth, nicNumber, operation};
    }

    private Path createWorkbook(Object[]... rows) throws IOException {
        Path workbookPath = Files.createTempFile("bulk-import-test-", ".xlsx");
        try (XSSFWorkbook workbook = new XSSFWorkbook(); OutputStream outputStream = Files.newOutputStream(workbookPath)) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Customers");
            CellStyle dateCellStyle = workbook.createCellStyle();
            CreationHelper creationHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd"));

            for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
                Row row = sheet.createRow(rowIndex);
                Object[] values = rows[rowIndex];
                for (int columnIndex = 0; columnIndex < values.length; columnIndex++) {
                    Object value = values[columnIndex];
                    if (value instanceof LocalDate) {
                        row.createCell(columnIndex).setCellValue(
                                Date.from(((LocalDate) value).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                        );
                        row.getCell(columnIndex).setCellStyle(dateCellStyle);
                    } else {
                        row.createCell(columnIndex).setCellValue(value == null ? "" : String.valueOf(value));
                    }
                }
            }

            workbook.write(outputStream);
        }
        return workbookPath;
    }
}
