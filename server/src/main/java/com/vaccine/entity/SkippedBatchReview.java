package com.vaccine.entity;

import com.vaccine.enums.BatchStatus;
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
@Table(name = "skipped_batch_reviews")
public class SkippedBatchReview {
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
    private String skipReason;

    @Column(nullable = false)
    private String skippedBy;

    @Column(nullable = false)
    private LocalDateTime skippedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus targetStatus;

    private String reviewComment;

    private String reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(nullable = false)
    private Boolean reviewed;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
