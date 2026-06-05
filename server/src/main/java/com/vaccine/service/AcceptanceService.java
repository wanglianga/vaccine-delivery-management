package com.vaccine.service;

import com.vaccine.dto.AcceptDeliveryRequest;
import com.vaccine.dto.ConfirmQuantityRequest;
import com.vaccine.dto.RejectDeliveryRequest;
import com.vaccine.entity.*;
import com.vaccine.enums.*;
import com.vaccine.exception.BusinessException;
import com.vaccine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AcceptanceService {

    @Autowired
    private AcceptanceRecordRepository acceptanceRecordRepository;

    @Autowired
    private DistributionOrderRepository distributionOrderRepository;

    @Autowired
    private VaccineBatchRepository vaccineBatchRepository;

    @Autowired
    private TemperatureEvidenceGapRepository evidenceGapRepository;

    @Autowired
    private ReturnTaskRepository returnTaskRepository;

    @Autowired
    private ClinicInventoryRepository clinicInventoryRepository;

    @Autowired
    private QuantityConfirmationRepository quantityConfirmationRepository;

    public List<AcceptanceRecord> getAllRecords() {
        return acceptanceRecordRepository.findAll();
    }

    public AcceptanceRecord acceptDelivery(Long id, AcceptDeliveryRequest request) {
        AcceptanceRecord record = acceptanceRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("验收记录不存在，ID: " + id));

        if (!Boolean.TRUE.equals(record.getSealIntact())) {
            throw new BusinessException("封签不完整，无法验收通过");
        }
        if (!Boolean.TRUE.equals(record.getTempCurveOk())) {
            throw new BusinessException("温度曲线异常，无法验收通过");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setReceivedQty(request.getReceivedQty());
        record.setStatus(AcceptanceStatus.ACCEPTED);
        record.setArrivalTime(now);
        record.setUpdatedAt(now);

        DistributionOrder order = distributionOrderRepository.findById(record.getDistributionOrderId())
                .orElseThrow(() -> new BusinessException("配送单不存在"));
        order.setStatus(OrderStatus.ACCEPTED);
        order.setUpdatedAt(now);
        distributionOrderRepository.save(order);

        VaccineBatch batch = vaccineBatchRepository.findById(order.getBatchId())
                .orElseThrow(() -> new BusinessException("疫苗批次不存在"));
        ClinicInventory inventory = ClinicInventory.builder()
                .batchId(batch.getId())
                .batchNo(batch.getBatchNo())
                .vaccineName(batch.getVaccineName())
                .quantity(request.getReceivedQty())
                .storageTempZone(batch.getStorageTempZone())
                .expiryDate(batch.getExpiryDate())
                .clinicName(order.getTargetClinic())
                .receivedAt(now)
                .build();
        clinicInventoryRepository.save(inventory);

        return acceptanceRecordRepository.save(record);
    }

    public AcceptanceRecord rejectDelivery(Long id, RejectDeliveryRequest request) {
        AcceptanceRecord record = acceptanceRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("验收记录不存在，ID: " + id));

        LocalDateTime now = LocalDateTime.now();
        record.setStatus(AcceptanceStatus.REJECTED);
        record.setRejectionReason(request.getReason());
        record.setUpdatedAt(now);

        ReturnTask returnTask = ReturnTask.builder()
                .acceptanceRecordId(id)
                .orderNo(record.getOrderNo())
                .reason(request.getReason())
                .status(ReturnTaskStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
        returnTask = returnTaskRepository.save(returnTask);
        record.setReturnTaskId(returnTask.getId());

        DistributionOrder order = distributionOrderRepository.findById(record.getDistributionOrderId())
                .orElseThrow(() -> new BusinessException("配送单不存在"));
        order.setStatus(OrderStatus.REJECTED);
        order.setUpdatedAt(now);
        distributionOrderRepository.save(order);

        return acceptanceRecordRepository.save(record);
    }

    public QuantityConfirmation confirmQuantity(Long id, ConfirmQuantityRequest request) {
        AcceptanceRecord record = acceptanceRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("验收记录不存在，ID: " + id));

        if (record.getStatus() != AcceptanceStatus.QTY_MISMATCH) {
            throw new BusinessException("当前验收状态不是数量不符，无需确认数量");
        }

        QuantityConfirmation confirmation = quantityConfirmationRepository
                .findByDistributionOrderId(record.getDistributionOrderId())
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    return QuantityConfirmation.builder()
                            .distributionOrderId(record.getDistributionOrderId())
                            .orderNo(record.getOrderNo())
                            .warehouseConfirmed(false)
                            .clinicConfirmed(false)
                            .status(QuantityConfirmationStatus.PENDING_BOTH)
                            .createdAt(now)
                            .build();
                });

        if ("warehouse".equals(request.getSide())) {
            confirmation.setWarehouseConfirmedQty(request.getQty());
            confirmation.setWarehouseConfirmed(true);
        } else if ("clinic".equals(request.getSide())) {
            confirmation.setClinicConfirmedQty(request.getQty());
            confirmation.setClinicConfirmed(true);
        } else {
            throw new BusinessException("无效的确认方，必须为 warehouse 或 clinic");
        }

        boolean wc = Boolean.TRUE.equals(confirmation.getWarehouseConfirmed());
        boolean cc = Boolean.TRUE.equals(confirmation.getClinicConfirmed());

        if (wc && cc) {
            confirmation.setStatus(QuantityConfirmationStatus.CONFIRMED);
            record.setStatus(AcceptanceStatus.PENDING);
            record.setWarehouseConfirmedQty(confirmation.getWarehouseConfirmedQty());
            record.setClinicConfirmedQty(confirmation.getClinicConfirmedQty());
            record.setUpdatedAt(LocalDateTime.now());
            acceptanceRecordRepository.save(record);
        } else if (wc) {
            confirmation.setStatus(QuantityConfirmationStatus.PENDING_CLINIC);
        } else if (cc) {
            confirmation.setStatus(QuantityConfirmationStatus.PENDING_WAREHOUSE);
        } else {
            confirmation.setStatus(QuantityConfirmationStatus.PENDING_BOTH);
        }

        return quantityConfirmationRepository.save(confirmation);
    }

    public AcceptanceRecord acknowledgeGap(Long id) {
        AcceptanceRecord record = acceptanceRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("验收记录不存在，ID: " + id));

        if (record.getStatus() != AcceptanceStatus.EVIDENCE_GAP) {
            throw new BusinessException("当前验收状态不是证据缺口，无法确认");
        }

        List<TemperatureEvidenceGap> gaps = evidenceGapRepository
                .findByDistributionOrderId(record.getDistributionOrderId());
        gaps.stream()
                .filter(g -> g.getStatus() == EvidenceGapStatus.OPEN || g.getStatus() == EvidenceGapStatus.CLOSED)
                .forEach(g -> {
                    g.setStatus(EvidenceGapStatus.ACKNOWLEDGED);
                    evidenceGapRepository.save(g);
                });

        record.setStatus(AcceptanceStatus.PENDING);
        record.setUpdatedAt(LocalDateTime.now());
        return acceptanceRecordRepository.save(record);
    }

    public AcceptanceRecord resumeInbound(Long id) {
        AcceptanceRecord record = acceptanceRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException("验收记录不存在，ID: " + id));

        if (record.getStatus() != AcceptanceStatus.TEMP_GAP) {
            throw new BusinessException("当前验收状态不是温度异常，无法恢复入库");
        }

        record.setTempCurveOk(true);
        record.setStatus(AcceptanceStatus.PENDING);
        record.setUpdatedAt(LocalDateTime.now());
        return acceptanceRecordRepository.save(record);
    }

    public List<ReturnTask> getReturnTasks() {
        return returnTaskRepository.findAll();
    }

    public List<ClinicInventory> getClinicInventory() {
        return clinicInventoryRepository.findAll();
    }

    public List<QuantityConfirmation> getQuantityConfirmations() {
        return quantityConfirmationRepository.findAll();
    }
}
