package com.vaccine.dto;

import lombok.Data;

@Data
public class ConfirmQuantityRequest {
    private String side;
    private Integer qty;
}
