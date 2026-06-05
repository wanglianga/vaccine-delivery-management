package com.vaccine.entity;

import com.vaccine.enums.SegmentStatus;
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
@Table(name = "transit_segments")
public class TransitSegment {
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

    private Integer segmentOrder;

    @Column(nullable = false)
    private String fromPoint;

    @Column(nullable = false)
    private String toPoint;

    @Column(nullable = false)
    private String vehicleNo;

    @Column(nullable = false)
    private String driverName;

    @Column(nullable = false)
    private String driverPhone;

    private LocalDateTime departTime;

    private LocalDateTime estimatedArrivalTime;

    private LocalDateTime actualArrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SegmentStatus status;

    private String delayReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
