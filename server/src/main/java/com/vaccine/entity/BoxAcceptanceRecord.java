package com.vaccine.entity;

import com.vaccine.enums.AcceptanceStatus;
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
@Table(name = "box_acceptance_records")
public class BoxAcceptanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long boxId;

    @Column(nullable = false)
    private String boxNo;

    @Column(nullable = false)
    private Long distributionOrderId;

    @Column(nullable = false)
    private String orderNo;

    @Column(nullable = false)
    private String batchNo;

    @Column(nullable = false)
    private String vaccineName;

    @Column(nullable = false)
    private Integer sentQty;

    @Column(nullable = false)
    private Integer receivedQty;

    @Column(nullable = false)
    private Boolean sealIntact;

    @Column(nullable = false)
    private Boolean tempCurveOk;

    private LocalDateTime arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcceptanceStatus status;

    private String rejectionReason;

    private String exceptionResponsibility;

    private Long returnTaskId;

    private Integer warehouseConfirmedQty;

    private Integer clinicConfirmedQty;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
