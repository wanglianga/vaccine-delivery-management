package com.vaccine.entity;

import com.vaccine.enums.BoxStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cold_chain_boxes")
public class ColdChainBox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String boxNo;

    @Column(nullable = false)
    private Long distributionOrderId;

    @Column(nullable = false)
    private String orderNo;

    @Column(nullable = false)
    private Long batchId;

    @Column(nullable = false)
    private String batchNo;

    @Column(nullable = false)
    private String vaccineName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String tempProbeNo;

    @Column(nullable = false)
    private String sealNo;

    @Column(nullable = false)
    private String targetClinic;

    private String currentVehicleNo;

    private String currentDriverName;

    private String currentDriverPhone;

    private String transferPoint;

    private LocalDateTime estimatedArrivalTime;

    private LocalDateTime actualArrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoxStatus status;

    private String exceptionRemark;

    private String responsibleParty;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
