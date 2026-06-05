package com.vaccine.dto;

import lombok.Data;

@Data
public class UpdateBoxStatusRequest {
    private String status;
    private String remark;
    private String responsibleParty;
}
