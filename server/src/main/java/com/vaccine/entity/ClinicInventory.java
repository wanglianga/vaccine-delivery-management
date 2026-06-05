package com.vaccine.entity;

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
@Table(name = "clinic_inventory")
public class ClinicInventory {
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
    private Integer quantity;

    @Column(nullable = false)
    private String storageTempZone;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private String clinicName;

    @Column(nullable = false)
    private LocalDateTime receivedAt;
}
