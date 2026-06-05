package com.vaccine.repository;

import com.vaccine.entity.DistributionOrder;
import com.vaccine.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistributionOrderRepository extends JpaRepository<DistributionOrder, Long> {

    List<DistributionOrder> findByStatus(OrderStatus status);
}
