package com.vaccine.repository;

import com.vaccine.entity.BoxAcceptanceRecord;
import com.vaccine.enums.AcceptanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoxAcceptanceRecordRepository extends JpaRepository<BoxAcceptanceRecord, Long> {
    Optional<BoxAcceptanceRecord> findByBoxId(Long boxId);
    List<BoxAcceptanceRecord> findByDistributionOrderId(Long distributionOrderId);
    List<BoxAcceptanceRecord> findByStatus(AcceptanceStatus status);
}
