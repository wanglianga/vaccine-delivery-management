package com.vaccine.dto;

import lombok.Data;
import java.util.List;

@Data
public class SplitBatchRequest {
    private Long batchId;
    private List<BoxSplitItem> boxes;

    @Data
    public static class BoxSplitItem {
        private Integer quantity;
        private String coldChainBoxNo;
        private String tempProbeNo;
        private String sealNo;
        private String vehicleNo;
        private String driverName;
        private String driverPhone;
        private String targetClinic;
        private String estimatedArrivalTime;
        private String transferPoint;
    }
}
