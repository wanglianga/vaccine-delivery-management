package com.vaccine.repository;

import com.vaccine.entity.TransitReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransitReportRepository extends JpaRepository<TransitReport, Long> {
    List<TransitReport> findByDistributionOrderIdOrderByReportTimeDesc(Long distributionOrderId);
}
