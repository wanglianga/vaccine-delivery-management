package com.vaccine.repository;

import com.vaccine.entity.QuantityConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuantityConfirmationRepository extends JpaRepository<QuantityConfirmation, Long> {
    Optional<QuantityConfirmation> findByDistributionOrderId(Long distributionOrderId);
}
