package com.vaccine.repository;

import com.vaccine.entity.BatchAdjustmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchAdjustmentRecordRepository extends JpaRepository<BatchAdjustmentRecord, Long> {
    List<BatchAdjustmentRecord> findByBatchIdOrderByCreatedAtDesc(Long batchId);
}
