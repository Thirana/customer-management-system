package com.example.customermanagement.importer;

import com.example.customermanagement.dto.response.ImportErrorDTO;
import com.example.customermanagement.dto.response.ImportStatusDTO;
import com.example.customermanagement.exception.ImportJobNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ImportProgressTracker {

    private final Map<String, ImportJobState> jobs = new ConcurrentHashMap<String, ImportJobState>();

    public ImportStatusDTO createJob(String jobId) {
        ImportJobState state = new ImportJobState(jobId);
        jobs.put(jobId, state);
        return toSnapshot(state);
    }

    public ImportStatusDTO getStatus(String jobId) {
        return toSnapshot(getRequiredState(jobId));
    }

    public void setTotalCount(String jobId, int totalCount) {
        ImportJobState state = getRequiredState(jobId);
        synchronized (state) {
            state.setTotalCount(totalCount);
        }
    }

    public void recordSuccesses(String jobId, int count) {
        ImportJobState state = getRequiredState(jobId);
        synchronized (state) {
            state.recordSuccesses(count);
        }
    }

    public void recordRowFailure(String jobId, int rowNumber, String message, int maxStoredErrors) {
        ImportJobState state = getRequiredState(jobId);
        synchronized (state) {
            state.recordRowFailure(rowNumber, message, maxStoredErrors);
        }
    }

    public void markCompleted(String jobId) {
        ImportJobState state = getRequiredState(jobId);
        synchronized (state) {
            state.markCompleted();
        }
    }

    public void markFailed(String jobId, String message, int maxStoredErrors) {
        ImportJobState state = getRequiredState(jobId);
        synchronized (state) {
            state.markFailed(message, maxStoredErrors);
        }
    }

    private ImportJobState getRequiredState(String jobId) {
        ImportJobState state = jobs.get(jobId);
        if (state == null) {
            throw new ImportJobNotFoundException(jobId);
        }
        return state;
    }

    private ImportStatusDTO toSnapshot(ImportJobState state) {
        synchronized (state) {
            ImportStatusDTO status = new ImportStatusDTO();
            status.setJobId(state.getJobId());
            status.setStatus(state.getStatus().name());
            status.setProcessedCount(state.getProcessedCount());
            status.setSuccessCount(state.getSuccessCount());
            status.setFailureCount(state.getFailureCount());
            status.setTotalCount(state.getTotalCount());
            status.setProgressPercent(state.getProgressPercent());
            status.setErrors(new ArrayList<ImportErrorDTO>(state.getErrors()));
            return status;
        }
    }

    private static final class ImportJobState {

        private final String jobId;
        private final List<ImportErrorDTO> errors = new ArrayList<ImportErrorDTO>();

        private ImportJobStatus status = ImportJobStatus.PROCESSING;
        private int processedCount;
        private int successCount;
        private int failureCount;
        private Integer totalCount;
        private int progressPercent;

        private ImportJobState(String jobId) {
            this.jobId = jobId;
        }

        public String getJobId() {
            return jobId;
        }

        public ImportJobStatus getStatus() {
            return status;
        }

        public int getProcessedCount() {
            return processedCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public Integer getTotalCount() {
            return totalCount;
        }

        public int getProgressPercent() {
            return progressPercent;
        }

        public List<ImportErrorDTO> getErrors() {
            return errors;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            updateProgress();
        }

        public void recordSuccesses(int count) {
            processedCount += count;
            successCount += count;
            updateProgress();
        }

        public void recordRowFailure(int rowNumber, String message, int maxStoredErrors) {
            processedCount++;
            failureCount++;
            if (errors.size() < maxStoredErrors) {
                errors.add(new ImportErrorDTO(rowNumber, message));
            }
            updateProgress();
        }

        public void markCompleted() {
            status = ImportJobStatus.COMPLETED;
            if (totalCount == null) {
                totalCount = processedCount;
            }
            if (totalCount != null && processedCount < totalCount) {
                processedCount = totalCount;
            }
            progressPercent = 100;
        }

        public void markFailed(String message, int maxStoredErrors) {
            status = ImportJobStatus.FAILED;
            if (message != null && !message.trim().isEmpty() && errors.size() < maxStoredErrors) {
                errors.add(new ImportErrorDTO(0, message));
            }
            updateProgress();
        }

        private void updateProgress() {
            if (totalCount == null || totalCount <= 0) {
                progressPercent = status == ImportJobStatus.COMPLETED ? 100 : 0;
                return;
            }

            progressPercent = Math.min(100, (int) ((processedCount * 100L) / totalCount));
        }
    }
}
