package com.vaccine.dto;

import lombok.Data;

@Data
public class TransitReportRequest {
    private Long distributionOrderId;
    private Double temperature;
    private Double latitude;
    private Double longitude;
    private String locationDesc;
    private String stopPoint;
    private String boxOpenRecord;
    private String probeStatus;
    private Integer probeBatteryLevel;
}
