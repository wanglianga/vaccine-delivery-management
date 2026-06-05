package com.vaccine.dto;

import lombok.Data;

@Data
public class SkipBatchRequest {
    private Long skippedBatchId;
    private Long selectedBatchId;
    private String skipReason;
    private String targetStatus;
    private String skippedBy;
    private String affectedClinics;
}
