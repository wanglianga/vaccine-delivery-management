package com.vaccine.repository;

import com.vaccine.entity.TransitSegment;
import com.vaccine.enums.SegmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransitSegmentRepository extends JpaRepository<TransitSegment, Long> {
    List<TransitSegment> findByBoxIdOrderBySegmentOrderAsc(Long boxId);
    List<TransitSegment> findByDistributionOrderId(Long distributionOrderId);
    List<TransitSegment> findByVehicleNo(String vehicleNo);
    List<TransitSegment> findByStatus(SegmentStatus status);
}
