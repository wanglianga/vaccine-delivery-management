package com.vaccine.dto;

import lombok.Data;

@Data
public class TransferBoxRequest {
    private Long boxId;
    private String fromPoint;
    private String toPoint;
    private String newVehicleNo;
    private String newDriverName;
    private String newDriverPhone;
    private String transferRemark;
}
