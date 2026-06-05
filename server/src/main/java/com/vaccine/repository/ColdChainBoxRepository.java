package com.vaccine.repository;

import com.vaccine.entity.ColdChainBox;
import com.vaccine.enums.BoxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColdChainBoxRepository extends JpaRepository<ColdChainBox, Long> {
    List<ColdChainBox> findByDistributionOrderId(Long distributionOrderId);
    List<ColdChainBox> findByBatchId(Long batchId);
    List<ColdChainBox> findByStatus(BoxStatus status);
    List<ColdChainBox> findByCurrentVehicleNo(String vehicleNo);
    List<ColdChainBox> findByTargetClinic(String targetClinic);
}
