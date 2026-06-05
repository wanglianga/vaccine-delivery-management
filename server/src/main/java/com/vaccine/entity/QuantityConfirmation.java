package com.vaccine.entity;

import com.vaccine.enums.QuantityConfirmationStatus;
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
@Table(name = "quantity_confirmations")
public class QuantityConfirmation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long distributionOrderId;

    @Column(nullable = false)
    private String orderNo;

    private Integer warehouseConfirmedQty;

    private Integer clinicConfirmedQty;

    @Column(nullable = false)
    private Boolean warehouseConfirmed;

    @Column(nullable = false)
    private Boolean clinicConfirmed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuantityConfirmationStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
