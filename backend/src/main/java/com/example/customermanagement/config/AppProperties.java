package com.example.customermanagement.config;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    // Centralizes custom application settings so invalid config fails during startup.
    @Valid
    private Frontend frontend = new Frontend();

    @Valid
    private ImportProperties importProperties = new ImportProperties();

    @Valid
    private Async async = new Async();

    public Frontend getFrontend() {
        return frontend;
    }

    public void setFrontend(Frontend frontend) {
        this.frontend = frontend;
    }

    public ImportProperties getImport() {
        return importProperties;
    }

    public void setImport(ImportProperties importProperties) {
        this.importProperties = importProperties;
    }

    public Async getAsync() {
        return async;
    }

    public void setAsync(Async async) {
        this.async = async;
    }

    public static class Frontend {

        // Used by CORS config; keep this specific instead of allowing all origins.
        @NotBlank
        private String allowedOrigin = "http://localhost:3000";

        public String getAllowedOrigin() {
            return allowedOrigin;
        }

        public void setAllowedOrigin(String allowedOrigin) {
            this.allowedOrigin = allowedOrigin;
        }
    }

    public static class ImportProperties {

        // Caps database writes per flush during Excel import.
        @Min(1)
        @Max(10000)
        private int batchSize = 500;

        // Mirrors Spring multipart limits and gives import services a typed value.
        @Min(1)
        private int maxFileSizeMb = 100;

        // Prevents long-running import jobs from keeping unbounded row errors in memory.
        @Min(1)
        private int maxStoredErrors = 100;

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public int getMaxFileSizeMb() {
            return maxFileSizeMb;
        }

        public void setMaxFileSizeMb(int maxFileSizeMb) {
            this.maxFileSizeMb = maxFileSizeMb;
        }

        public int getMaxStoredErrors() {
            return maxStoredErrors;
        }

        public void setMaxStoredErrors(int maxStoredErrors) {
            this.maxStoredErrors = maxStoredErrors;
        }
    }

    public static class Async {

        // Import jobs use a bounded pool so uploads cannot exhaust server threads.
        @Min(1)
        private int corePoolSize = 2;

        @Min(1)
        private int maxPoolSize = 5;

        @Min(1)
        private int queueCapacity = 10;

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        @AssertTrue(message = "maxPoolSize must be greater than or equal to corePoolSize")
        public boolean isPoolSizeValid() {
            return maxPoolSize >= corePoolSize;
        }
    }
}
