package com.example.customermanagement.tool;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Generates assignment-friendly customer import workbooks without loading the entire sheet into memory.
 */
public final class ImportWorkbookGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private ImportWorkbookGenerator() {
    }

    public static void main(String[] args) throws Exception {
        GeneratorOptions options = GeneratorOptions.parse(args);
        generateWorkbook(options);
    }

    private static void generateWorkbook(GeneratorOptions options) throws IOException {
        Path outputPath = options.getOutputPath();
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        try {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Customers");
            writeHeader(sheet.createRow(0));

            for (int rowIndex = 0; rowIndex < options.getRowCount(); rowIndex++) {
                writeGeneratedRow(sheet.createRow(rowIndex + 1), rowIndex, options.getMode());
            }

            if (options.isIncludeInvalidRow()) {
                writeInvalidRow(sheet.createRow(options.getRowCount() + 1), options.getRowCount());
            }

            FileOutputStream outputStream = new FileOutputStream(outputPath.toFile());
            try {
                workbook.write(outputStream);
            } finally {
                outputStream.close();
            }
        } finally {
            workbook.dispose();
            workbook.close();
        }

        System.out.println("Generated workbook at " + outputPath.toAbsolutePath());
        System.out.println("Rows: " + options.getRowCount() + (options.isIncludeInvalidRow() ? " + 1 invalid row" : ""));
        System.out.println("Mode: " + options.getMode().getCliValue());
    }

    private static void writeHeader(Row row) {
        row.createCell(0).setCellValue("Name");
        row.createCell(1).setCellValue("Date of Birth");
        row.createCell(2).setCellValue("NIC Number");
        row.createCell(3).setCellValue("Operation");
    }

    private static void writeGeneratedRow(Row row, int rowIndex, GeneratorMode mode) {
        GeneratedImportRow generatedRow = mode.generate(rowIndex);
        row.createCell(0).setCellValue(generatedRow.getName());
        row.createCell(1).setCellValue(generatedRow.getDateOfBirth().format(DATE_FORMATTER));
        row.createCell(2).setCellValue(generatedRow.getNicNumber());
        row.createCell(3).setCellValue(generatedRow.getOperation());
    }

    private static void writeInvalidRow(Row row, int rowCount) {
        row.createCell(0).setCellValue("");
        row.createCell(1).setCellValue(LocalDate.of(1995, 1, 1).format(DATE_FORMATTER));
        row.createCell(2).setCellValue(String.format("GEN-INVALID-%06d", rowCount + 1));
        row.createCell(3).setCellValue("CREATE");
    }

    private enum GeneratorMode {
        CREATE_ONLY("create-only") {
            @Override
            GeneratedImportRow generate(int rowIndex) {
                int displayNumber = rowIndex + 1;
                return new GeneratedImportRow(
                        "Generated Create Customer " + displayNumber,
                        dateOfBirth(rowIndex),
                        String.format("GEN-CREATE-%06d", displayNumber),
                        "CREATE"
                );
            }
        },
        AUTO("auto") {
            @Override
            GeneratedImportRow generate(int rowIndex) {
                int displayNumber = rowIndex + 1;
                return new GeneratedImportRow(
                        "Generated Auto Customer " + displayNumber,
                        dateOfBirth(rowIndex),
                        String.format("GEN-AUTO-%06d", displayNumber),
                        ""
                );
            }
        },
        MIXED("mixed") {
            @Override
            GeneratedImportRow generate(int rowIndex) {
                int displayNumber = rowIndex + 1;
                int variant = rowIndex % 3;
                if (variant == 0) {
                    return new GeneratedImportRow(
                            "Generated Mixed Create " + displayNumber,
                            dateOfBirth(rowIndex),
                            String.format("GEN-MIX-CREATE-%06d", displayNumber),
                            "CREATE"
                    );
                }
                if (variant == 1) {
                    int seededIndex = (rowIndex % 8) + 1;
                    return new GeneratedImportRow(
                            "Updated Seed Customer " + seededIndex + " Batch " + displayNumber,
                            dateOfBirth(rowIndex),
                            String.format("DEV-NIC-%03d", seededIndex),
                            ""
                    );
                }
                return new GeneratedImportRow(
                        "Generated Mixed Update " + displayNumber,
                        dateOfBirth(rowIndex),
                        String.format("DEV-NIC-%03d", ((rowIndex + 1) % 8) + 1),
                        "UPDATE"
                );
            }
        };

        private final String cliValue;

        GeneratorMode(String cliValue) {
            this.cliValue = cliValue;
        }

        public String getCliValue() {
            return cliValue;
        }

        abstract GeneratedImportRow generate(int rowIndex);

        static GeneratorMode fromCliValue(String value) {
            for (GeneratorMode mode : values()) {
                if (mode.getCliValue().equals(value)) {
                    return mode;
                }
            }
            throw new IllegalArgumentException("Unsupported mode: " + value
                    + ". Expected one of create-only, auto, or mixed.");
        }

        private static LocalDate dateOfBirth(int rowIndex) {
            return LocalDate.of(1980, 1, 1).plusDays(rowIndex % 10000);
        }
    }

    private static final class GeneratedImportRow {

        private final String name;
        private final LocalDate dateOfBirth;
        private final String nicNumber;
        private final String operation;

        private GeneratedImportRow(String name, LocalDate dateOfBirth, String nicNumber, String operation) {
            this.name = name;
            this.dateOfBirth = dateOfBirth;
            this.nicNumber = nicNumber;
            this.operation = operation;
        }

        public String getName() {
            return name;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public String getNicNumber() {
            return nicNumber;
        }

        public String getOperation() {
            return operation;
        }
    }

    private static final class GeneratorOptions {

        private final Path outputPath;
        private final int rowCount;
        private final GeneratorMode mode;
        private final boolean includeInvalidRow;

        private GeneratorOptions(Path outputPath, int rowCount, GeneratorMode mode, boolean includeInvalidRow) {
            this.outputPath = outputPath;
            this.rowCount = rowCount;
            this.mode = mode;
            this.includeInvalidRow = includeInvalidRow;
        }

        public Path getOutputPath() {
            return outputPath;
        }

        public int getRowCount() {
            return rowCount;
        }

        public GeneratorMode getMode() {
            return mode;
        }

        public boolean isIncludeInvalidRow() {
            return includeInvalidRow;
        }

        static GeneratorOptions parse(String[] args) {
            Path outputPath = null;
            int rowCount = 1000;
            GeneratorMode mode = GeneratorMode.MIXED;
            boolean includeInvalidRow = false;

            for (String arg : args) {
                if (arg.startsWith("--output=")) {
                    outputPath = Paths.get(arg.substring("--output=".length()));
                } else if (arg.startsWith("--rows=")) {
                    rowCount = parseRowCount(arg.substring("--rows=".length()));
                } else if (arg.startsWith("--mode=")) {
                    mode = GeneratorMode.fromCliValue(arg.substring("--mode=".length()).toLowerCase(Locale.ROOT));
                } else if (arg.startsWith("--include-invalid-row=")) {
                    includeInvalidRow = Boolean.parseBoolean(arg.substring("--include-invalid-row=".length()));
                } else {
                    throw new IllegalArgumentException("Unsupported argument: " + arg);
                }
            }

            if (outputPath == null) {
                throw new IllegalArgumentException("Missing required argument: --output=<path>");
            }

            return new GeneratorOptions(outputPath, rowCount, mode, includeInvalidRow);
        }

        private static int parseRowCount(String value) {
            int parsed = Integer.parseInt(value);
            if (parsed < 1) {
                throw new IllegalArgumentException("--rows must be greater than zero");
            }
            return parsed;
        }
    }
}
