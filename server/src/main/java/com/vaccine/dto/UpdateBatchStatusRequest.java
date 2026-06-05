package com.vaccine.dto;

import lombok.Data;

@Data
public class UpdateBatchStatusRequest {
    private String status;
    private String operator;
    private String remark;
}
