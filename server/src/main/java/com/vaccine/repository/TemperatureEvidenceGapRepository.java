package com.vaccine.repository;

import com.vaccine.entity.TemperatureEvidenceGap;
import com.vaccine.enums.EvidenceGapStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemperatureEvidenceGapRepository extends JpaRepository<TemperatureEvidenceGap, Long> {
    List<TemperatureEvidenceGap> findByDistributionOrderId(Long distributionOrderId);

    Optional<TemperatureEvidenceGap> findFirstByDistributionOrderIdAndProbeNoAndStatusOrderByOfflineAtDesc(
            Long distributionOrderId, String probeNo, EvidenceGapStatus status);
}
