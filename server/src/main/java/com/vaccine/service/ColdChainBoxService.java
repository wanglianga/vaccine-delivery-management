package com.vaccine.service;

import com.vaccine.dto.*;
import com.vaccine.entity.*;
import com.vaccine.enums.*;
import com.vaccine.exception.BusinessException;
import com.vaccine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ColdChainBoxService {

    @Autowired
    private ColdChainBoxRepository coldChainBoxRepository;

    @Autowired
    private TransitSegmentRepository transitSegmentRepository;

    @Autowired
    private BoxAcceptanceRecordRepository boxAcceptanceRecordRepository;

    @Autowired
    private DistributionOrderRepository distributionOrderRepository;

    @Autowired
    private VaccineBatchRepository vaccineBatchRepository;

    @Autowired
    private ClinicInventoryRepository clinicInventoryRepository;

    @Autowired
    private ReturnTaskRepository returnTaskRepository;

    public List<ColdChainBox> getAllBoxes() {
        return coldChainBoxRepository.findAll();
    }

    public List<ColdChainBox> getBoxesByBatchId(Long batchId) {
        return coldChainBoxRepository.findByBatchId(batchId);
    }

    public List<ColdChainBox> getBoxesByOrderId(Long orderId) {
        return coldChainBoxRepository.findByDistributionOrderId(orderId);
    }

    public ColdChainBox getBoxById(Long id) {
        return coldChainBoxRepository.findById(id)
                .orElseThrow(() -> new BusinessException("冷链箱不存在，ID: " + id));
    }

    public List<TransitSegment> getSegmentsByBoxId(Long boxId) {
        return transitSegmentRepository.findByBoxIdOrderBySegmentOrderAsc(boxId);
    }

    public BoxAcceptanceRecord getAcceptanceByBoxId(Long boxId) {
        return boxAcceptanceRecordRepository.findByBoxId(boxId)
                .orElseThrow(() -> new BusinessException("箱子验收记录不存在，箱子ID: " + boxId));
    }

    @Transactional
    public List<ColdChainBox> splitBatch(SplitBatchRequest request) {
        LocalDateTime now = LocalDateTime.now();
        VaccineBatch batch = vaccineBatchRepository.findById(request.getBatchId())
                .orElseThrow(() -> new BusinessException("疫苗批次不存在，ID: " + request.getBatchId()));

        int totalSplitQty = request.getBoxes().stream()
                .mapToInt(SplitBatchRequest.BoxSplitItem::getQuantity)
                .sum();

        if (totalSplitQty > batch.getDistributableQty()) {
            throw new BusinessException("拆分总数量超过可配送数量，可配送: " + batch.getDistributableQty());
        }

        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long todayOrderCount = distributionOrderRepository.count();
        String orderNo = String.format("DIS-%s-%03d", datePart, todayOrderCount + 1);

        DistributionOrder order = DistributionOrder.builder()
                .orderNo(orderNo)
                .batchId(batch.getId())
                .batchNo(batch.getBatchNo())
                .vaccineName(batch.getVaccineName())
                .quantity(totalSplitQty)
                .coldChainBoxNo("MULTI-BOX")
                .tempProbeNo("MULTI-PROBE")
                .sealNo("MULTI-SEAL")
                .vehicleNo("MULTI-VEHICLE")
                .driverName("多车配送")
                .driverPhone("-")
                .targetClinic("多门诊拆分")
                .status(OrderStatus.OUTBOUND)
                .createdAt(now)
                .updatedAt(now)
                .build();
        order = distributionOrderRepository.save(order);

        List<ColdChainBox> boxes = new ArrayList<>();
        int boxIndex = 1;

        for (SplitBatchRequest.BoxSplitItem item : request.getBoxes()) {
            String boxNo = String.format("BOX-%s-%03d", datePart, todayOrderCount * 10 + boxIndex);
            boxIndex++;

            LocalDateTime estimatedArrivalTime = null;
            if (item.getEstimatedArrivalTime() != null && !item.getEstimatedArrivalTime().isEmpty()) {
                estimatedArrivalTime = LocalDateTime.parse(item.getEstimatedArrivalTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }

            ColdChainBox box = ColdChainBox.builder()
                    .boxNo(boxNo)
                    .distributionOrderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .batchId(batch.getId())
                    .batchNo(batch.getBatchNo())
                    .vaccineName(batch.getVaccineName())
                    .quantity(item.getQuantity())
                    .tempProbeNo(item.getTempProbeNo())
                    .sealNo(item.getSealNo())
                    .targetClinic(item.getTargetClinic())
                    .currentVehicleNo(item.getVehicleNo())
                    .currentDriverName(item.getDriverName())
                    .currentDriverPhone(item.getDriverPhone())
                    .transferPoint(item.getTransferPoint())
                    .estimatedArrivalTime(estimatedArrivalTime)
                    .status(BoxStatus.OUTBOUND)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            box = coldChainBoxRepository.save(box);
            boxes.add(box);

            TransitSegment firstSegment = TransitSegment.builder()
                    .boxId(box.getId())
                    .boxNo(box.getBoxNo())
                    .distributionOrderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .segmentOrder(1)
                    .fromPoint("疾控中心仓库")
                    .toPoint(item.getTransferPoint() != null ? item.getTransferPoint() : item.getTargetClinic())
                    .vehicleNo(item.getVehicleNo())
                    .driverName(item.getDriverName())
                    .driverPhone(item.getDriverPhone())
                    .departTime(now)
                    .estimatedArrivalTime(estimatedArrivalTime)
                    .status(SegmentStatus.IN_PROGRESS)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            transitSegmentRepository.save(firstSegment);

            BoxAcceptanceRecord acceptance = BoxAcceptanceRecord.builder()
                    .boxId(box.getId())
                    .boxNo(box.getBoxNo())
                    .distributionOrderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .batchNo(batch.getBatchNo())
                    .vaccineName(batch.getVaccineName())
                    .sentQty(item.getQuantity())
                    .receivedQty(0)
                    .sealIntact(true)
                    .tempCurveOk(true)
                    .status(AcceptanceStatus.PENDING)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            boxAcceptanceRecordRepository.save(acceptance);
        }

        batch.setDistributableQty(batch.getDistributableQty() - totalSplitQty);
        if (batch.getDistributableQty() == 0) {
            batch.setStatus(BatchStatus.EXHAUSTED);
        } else {
            batch.setStatus(BatchStatus.PARTIAL);
        }
        batch.setUpdatedAt(now);
        vaccineBatchRepository.save(batch);

        return boxes;
    }

    @Transactional
    public TransitSegment transferBox(TransferBoxRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ColdChainBox box = coldChainBoxRepository.findById(request.getBoxId())
                .orElseThrow(() -> new BusinessException("冷链箱不存在，ID: " + request.getBoxId()));

        List<TransitSegment> segments = transitSegmentRepository.findByBoxIdOrderBySegmentOrderAsc(box.getId());
        TransitSegment lastSegment = segments.isEmpty() ? null : segments.get(segments.size() - 1);

        if (lastSegment != null) {
            lastSegment.setActualArrivalTime(now);
            lastSegment.setStatus(SegmentStatus.COMPLETED);
            lastSegment.setUpdatedAt(now);
            transitSegmentRepository.save(lastSegment);
        }

        int nextSegmentOrder = segments.size() + 1;

        TransitSegment newSegment = TransitSegment.builder()
                .boxId(box.getId())
                .boxNo(box.getBoxNo())
                .distributionOrderId(box.getDistributionOrderId())
                .orderNo(box.getOrderNo())
                .segmentOrder(nextSegmentOrder)
                .fromPoint(request.getFromPoint())
                .toPoint(request.getToPoint())
                .vehicleNo(request.getNewVehicleNo())
                .driverName(request.getNewDriverName())
                .driverPhone(request.getNewDriverPhone())
                .departTime(now)
                .status(SegmentStatus.IN_PROGRESS)
                .createdAt(now)
                .updatedAt(now)
                .build();
        newSegment = transitSegmentRepository.save(newSegment);

        box.setCurrentVehicleNo(request.getNewVehicleNo());
        box.setCurrentDriverName(request.getNewDriverName());
        box.setCurrentDriverPhone(request.getNewDriverPhone());
        box.setTransferPoint(request.getFromPoint());
        box.setStatus(BoxStatus.TRANSFERRING);
        box.setUpdatedAt(now);
        coldChainBoxRepository.save(box);

        return newSegment;
    }

    @Transactional
    public BoxAcceptanceRecord acceptBox(Long boxId, BoxAcceptanceRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ColdChainBox box = coldChainBoxRepository.findById(boxId)
                .orElseThrow(() -> new BusinessException("冷链箱不存在，ID: " + boxId));

        BoxAcceptanceRecord record = boxAcceptanceRecordRepository.findByBoxId(boxId)
                .orElseThrow(() -> new BusinessException("验收记录不存在，箱子ID: " + boxId));

        if (!Boolean.TRUE.equals(request.getSealIntact())) {
            throw new BusinessException("封签不完整，无法验收通过");
        }
        if (!Boolean.TRUE.equals(request.getTempCurveOk())) {
            throw new BusinessException("温度曲线异常，无法验收通过");
        }

        record.setReceivedQty(request.getReceivedQty());
        record.setSealIntact(request.getSealIntact());
        record.setTempCurveOk(request.getTempCurveOk());
        record.setStatus(AcceptanceStatus.ACCEPTED);
        record.setArrivalTime(now);
        record.setExceptionResponsibility(request.getExceptionResponsibility());
        record.setUpdatedAt(now);
        boxAcceptanceRecordRepository.save(record);

        box.setStatus(BoxStatus.ACCEPTED);
        box.setActualArrivalTime(now);
        box.setUpdatedAt(now);
        coldChainBoxRepository.save(box);

        List<TransitSegment> segments = transitSegmentRepository.findByBoxIdOrderBySegmentOrderAsc(boxId);
        for (TransitSegment segment : segments) {
            if (segment.getStatus() == SegmentStatus.IN_PROGRESS) {
                segment.setActualArrivalTime(now);
                segment.setStatus(SegmentStatus.COMPLETED);
                segment.setUpdatedAt(now);
                transitSegmentRepository.save(segment);
            }
        }

        VaccineBatch batch = vaccineBatchRepository.findById(box.getBatchId())
                .orElseThrow(() -> new BusinessException("疫苗批次不存在"));
        ClinicInventory inventory = ClinicInventory.builder()
                .batchId(batch.getId())
                .batchNo(batch.getBatchNo())
                .vaccineName(batch.getVaccineName())
                .quantity(request.getReceivedQty())
                .storageTempZone(batch.getStorageTempZone())
                .expiryDate(batch.getExpiryDate())
                .clinicName(box.getTargetClinic())
                .receivedAt(now)
                .build();
        clinicInventoryRepository.save(inventory);

        return record;
    }

    @Transactional
    public BoxAcceptanceRecord rejectBox(Long boxId, BoxAcceptanceRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ColdChainBox box = coldChainBoxRepository.findById(boxId)
                .orElseThrow(() -> new BusinessException("冷链箱不存在，ID: " + boxId));

        BoxAcceptanceRecord record = boxAcceptanceRecordRepository.findByBoxId(boxId)
                .orElseThrow(() -> new BusinessException("验收记录不存在，箱子ID: " + boxId));

        record.setStatus(AcceptanceStatus.REJECTED);
        record.setRejectionReason(request.getRejectionReason());
        record.setExceptionResponsibility(request.getExceptionResponsibility());
        record.setUpdatedAt(now);
        record = boxAcceptanceRecordRepository.save(record);

        ReturnTask returnTask = ReturnTask.builder()
                .acceptanceRecordId(record.getId())
                .orderNo(record.getOrderNo())
                .reason(request.getRejectionReason())
                .status(ReturnTaskStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        returnTask = returnTaskRepository.save(returnTask);
        record.setReturnTaskId(returnTask.getId());
        boxAcceptanceRecordRepository.save(record);

        box.setStatus(BoxStatus.REJECTED);
        box.setExceptionRemark(request.getRejectionReason());
        box.setResponsibleParty(request.getExceptionResponsibility());
        box.setUpdatedAt(now);
        coldChainBoxRepository.save(box);

        return record;
    }

    @Transactional
    public ColdChainBox updateBoxStatus(Long boxId, UpdateBoxStatusRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ColdChainBox box = coldChainBoxRepository.findById(boxId)
                .orElseThrow(() -> new BusinessException("冷链箱不存在，ID: " + boxId));

        BoxStatus newStatus = BoxStatus.valueOf(request.getStatus().toUpperCase());
        box.setStatus(newStatus);

        if (request.getRemark() != null) {
            box.setExceptionRemark(request.getRemark());
        }
        if (request.getResponsibleParty() != null) {
            box.setResponsibleParty(request.getResponsibleParty());
        }

        box.setUpdatedAt(now);
        return coldChainBoxRepository.save(box);
    }

    @Transactional
    public void markVehicleDelayed(String vehicleNo, String delayReason) {
        LocalDateTime now = LocalDateTime.now();
        List<ColdChainBox> boxes = coldChainBoxRepository.findByCurrentVehicleNo(vehicleNo);

        for (ColdChainBox box : boxes) {
            box.setStatus(BoxStatus.DELAYED);
            box.setExceptionRemark(delayReason);
            box.setUpdatedAt(now);
            coldChainBoxRepository.save(box);

            List<TransitSegment> segments = transitSegmentRepository.findByBoxIdOrderBySegmentOrderAsc(box.getId());
            for (TransitSegment segment : segments) {
                if (segment.getStatus() == SegmentStatus.IN_PROGRESS) {
                    segment.setStatus(SegmentStatus.DELAYED);
                    segment.setDelayReason(delayReason);
                    segment.setUpdatedAt(now);
                    transitSegmentRepository.save(segment);
                }
            }
        }
    }

    public List<ColdChainBox> getDelayedBoxes() {
        return coldChainBoxRepository.findByStatus(BoxStatus.DELAYED);
    }
}
