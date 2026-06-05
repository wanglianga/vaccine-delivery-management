package com.vaccine.entity;

import com.vaccine.enums.EvidenceGapStatus;
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
@Table(name = "temperature_evidence_gaps")
public class TemperatureEvidenceGap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long distributionOrderId;

    @Column(nullable = false)
    private String orderNo;

    @Column(nullable = false)
    private String probeNo;

    @Column(nullable = false)
    private LocalDateTime offlineAt;

    private LocalDateTime backOnlineAt;

    private Long duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvidenceGapStatus status;

    @Column(nullable = false)
    private String description;
}
