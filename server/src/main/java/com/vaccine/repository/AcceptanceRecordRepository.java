package com.vaccine.repository;

import com.vaccine.entity.AcceptanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcceptanceRecordRepository extends JpaRepository<AcceptanceRecord, Long> {
}
