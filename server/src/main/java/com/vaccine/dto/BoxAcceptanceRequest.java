package com.vaccine.dto;

import lombok.Data;

@Data
public class BoxAcceptanceRequest {
    private Integer receivedQty;
    private Boolean sealIntact;
    private Boolean tempCurveOk;
    private String rejectionReason;
    private String exceptionResponsibility;
}
