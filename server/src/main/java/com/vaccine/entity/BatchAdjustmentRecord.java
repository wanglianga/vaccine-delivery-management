package com.vaccine.entity;

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
@Table(name = "batch_adjustment_records")
public class BatchAdjustmentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long batchId;

    @Column(nullable = false)
    private String batchNo;

    @Column(nullable = false)
    private Integer recommendedOrder;

    @Column(nullable = false)
    private Integer actualOrder;

    private String skipReason;

    private String adjustedBy;

    private String affectedClinics;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
