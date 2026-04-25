package com.example.customermanagement.dto.response;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImportStatusDTO {

    private String jobId;

    private String status;

    private int processedCount;

    private int successCount;

    private int failureCount;

    private Integer totalCount;

    private int progressPercent;

    private List<ImportErrorDTO> errors = new ArrayList<ImportErrorDTO>();
}
