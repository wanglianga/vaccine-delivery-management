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
@Table(name = "distribution_recommendations")
public class DistributionRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long batchId;

    @Column(nullable = false)
    private String batchNo;

    @Column(nullable = false)
    private String vaccineName;

    @Column(nullable = false)
    private Integer recommendedOrder;

    @Column(nullable = false)
    private Double expiryScore;

    @Column(nullable = false)
    private Double appointmentScore;

    @Column(nullable = false)
    private Double inventoryScore;

    @Column(nullable = false)
    private Double distanceScore;

    @Column(nullable = false)
    private Double capacityScore;

    @Column(nullable = false)
    private Double totalScore;

    @Column(nullable = false)
    private Long daysToExpiry;

    @Column(nullable = false)
    private Integer appointmentCount;

    private String targetClinic;

    private Double distanceKm;

    private Integer coldChainBoxRemaining;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String createdBy;
}
