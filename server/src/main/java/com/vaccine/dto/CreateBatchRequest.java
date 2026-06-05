package com.vaccine.dto;

import lombok.Data;

@Data
public class CreateBatchRequest {
    private String batchNo;
    private String vaccineName;
    private String manufacturer;
    private String specification;
    private String expiryDate;
    private String storageTempZone;
    private String batchReleaseDoc;
    private Integer totalQty;
    private Integer distributableQty;
}
