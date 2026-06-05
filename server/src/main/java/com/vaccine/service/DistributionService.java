package com.vaccine.service;

import com.vaccine.dto.CreateDistributionRequest;
import com.vaccine.entity.DistributionOrder;
import com.vaccine.entity.VaccineBatch;
import com.vaccine.enums.OrderStatus;
import com.vaccine.exception.BusinessException;
import com.vaccine.repository.DistributionOrderRepository;
import com.vaccine.repository.VaccineBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DistributionService {

    @Autowired
    private DistributionOrderRepository distributionOrderRepository;

    @Autowired
    private VaccineBatchRepository vaccineBatchRepository;

    public List<DistributionOrder> getAllOrders() {
        return distributionOrderRepository.findAll();
    }

    public List<DistributionOrder> getOrdersByStatus(String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return distributionOrderRepository.findByStatus(orderStatus);
    }

    public DistributionOrder createOrder(CreateDistributionRequest request) {
        LocalDateTime now = LocalDateTime.now();

        VaccineBatch batch = vaccineBatchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new BusinessException("疫苗批次不存在，ID: " + request.getBatchId()));

        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long todayCount = distributionOrderRepository.count();
        String orderNo = String.format("DIS-%s-%03d", datePart, todayCount + 1);

        LocalDateTime estimatedArrivalTime = null;
        if (request.getEstimatedArrivalTime() != null && !request.getEstimatedArrivalTime().isEmpty()) {
            estimatedArrivalTime = LocalDateTime.parse(request.getEstimatedArrivalTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        DistributionOrder order = DistributionOrder.builder()
                .orderNo(orderNo)
                .batchId(batch.getId())
                .batchNo(batch.getBatchNo())
                .vaccineName(batch.getVaccineName())
                .quantity(request.getQuantity())
                .coldChainBoxNo(request.getColdChainBoxNo())
                .tempProbeNo(request.getTempProbeNo())
                .sealNo(request.getSealNo())
                .vehicleNo(request.getVehicleNo())
                .driverName(request.getDriverName())
                .driverPhone(request.getDriverPhone())
                .estimatedArrivalTime(estimatedArrivalTime)
                .targetClinic(request.getTargetClinic())
                .status(OrderStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return distributionOrderRepository.save(order);
    }

    public DistributionOrder updateOrderStatus(Long id, String status) {
        DistributionOrder order = distributionOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("配送单不存在，ID: " + id));

        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        order.setUpdatedAt(LocalDateTime.now());
        return distributionOrderRepository.save(order);
    }
}
