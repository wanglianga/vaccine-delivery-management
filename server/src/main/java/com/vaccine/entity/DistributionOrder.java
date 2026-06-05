package com.vaccine.entity;

import com.vaccine.enums.OrderStatus;
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
@Table(name = "distribution_orders")
public class DistributionOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
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
    private String coldChainBoxNo;

    @Column(nullable = false)
    private String tempProbeNo;

    @Column(nullable = false)
    private String sealNo;

    @Column(nullable = false)
    private String vehicleNo;

    @Column(nullable = false)
    private String driverName;

    @Column(nullable = false)
    private String driverPhone;

    private LocalDateTime estimatedArrivalTime;

    @Column(nullable = false)
    private String targetClinic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
