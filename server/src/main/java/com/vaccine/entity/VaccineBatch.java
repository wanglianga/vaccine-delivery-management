package com.vaccine.entity;

import com.vaccine.enums.BatchStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vaccine_batches")
public class VaccineBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String batchNo;

    @Column(nullable = false)
    private String vaccineName;

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private String specification;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private String storageTempZone;

    @Column(nullable = false)
    private String batchReleaseDoc;

    @Column(nullable = false)
    private Integer totalQty;

    @Column(nullable = false)
    private Integer distributableQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
