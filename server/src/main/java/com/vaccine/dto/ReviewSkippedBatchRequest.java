package com.vaccine.dto;

import lombok.Data;

@Data
public class ReviewSkippedBatchRequest {
    private String reviewComment;
    private String reviewedBy;
    private Boolean approved;
}
