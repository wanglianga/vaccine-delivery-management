package com.vaccine.dto;

import lombok.Data;

@Data
public class CreateDistributionRequest {
    private Long batchId;
    private Integer quantity;
    private String coldChainBoxNo;
    private String tempProbeNo;
    private String sealNo;
    private String vehicleNo;
    private String driverName;
    private String driverPhone;
    private String estimatedArrivalTime;
    private String targetClinic;
}
