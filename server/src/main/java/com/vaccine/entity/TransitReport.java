package com.vaccine.entity;

import com.vaccine.enums.ProbeStatus;
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
@Table(name = "transit_reports")
public class TransitReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long distributionOrderId;

    @Column(nullable = false)
    private String orderNo;

    @Column(nullable = false)
    private LocalDateTime reportTime;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String locationDesc;

    private String stopPoint;

    private String boxOpenRecord;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProbeStatus probeStatus;

    @Column(nullable = false)
    private Integer probeBatteryLevel;
}
