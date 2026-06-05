package com.vaccine.dto;

import lombok.Data;

import java.util.List;

@Data
public class GenerateRecommendationRequest {
    private String vaccineName;
    private String targetClinic;
    private Double distanceKm;
    private Integer coldChainBoxCapacity;
    private Integer coldChainBoxUsed;
    private List<Long> batchIds;
    private String operator;
}
