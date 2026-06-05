package com.vaccine.service;

import com.vaccine.dto.TransitReportRequest;
import com.vaccine.entity.DistributionOrder;
import com.vaccine.entity.TemperatureEvidenceGap;
import com.vaccine.entity.TransitReport;
import com.vaccine.enums.EvidenceGapStatus;
import com.vaccine.enums.ProbeStatus;
import com.vaccine.exception.BusinessException;
import com.vaccine.repository.DistributionOrderRepository;
import com.vaccine.repository.TemperatureEvidenceGapRepository;
import com.vaccine.repository.TransitReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransitService {

    @Autowired
    private TransitReportRepository transitReportRepository;

    @Autowired
    private TemperatureEvidenceGapRepository evidenceGapRepository;

    @Autowired
    private DistributionOrderRepository distributionOrderRepository;

    public List<TransitReport> getAllReports() {
        return transitReportRepository.findAll();
    }

    public List<TransitReport> getReportsByOrderId(String orderId) {
        Long id = Long.valueOf(orderId);
        return transitReportRepository.findByDistributionOrderIdOrderByReportTimeDesc(id);
    }

    public TransitReport submitReport(TransitReportRequest request) {
        LocalDateTime now = LocalDateTime.now();

        DistributionOrder order = distributionOrderRepository.findById(request.getDistributionOrderId())
                .orElseThrow(() -> new BusinessException("配送单不存在，ID: " + request.getDistributionOrderId()));

        ProbeStatus probeStatus = ProbeStatus.valueOf(request.getProbeStatus().toUpperCase());

        TransitReport report = TransitReport.builder()
                .distributionOrderId(request.getDistributionOrderId())
                .orderNo(order.getOrderNo())
                .reportTime(now)
                .temperature(request.getTemperature())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationDesc(request.getLocationDesc())
                .stopPoint(request.getStopPoint())
                .boxOpenRecord(request.getBoxOpenRecord())
                .probeStatus(probeStatus)
                .probeBatteryLevel(request.getProbeBatteryLevel())
                .build();

        report = transitReportRepository.save(report);

        if (probeStatus == ProbeStatus.OFFLINE) {
            TemperatureEvidenceGap gap = TemperatureEvidenceGap.builder()
                    .distributionOrderId(request.getDistributionOrderId())
                    .orderNo(order.getOrderNo())
                    .probeNo(order.getTempProbeNo())
                    .offlineAt(now)
                    .status(EvidenceGapStatus.OPEN)
                    .description("探头" + order.getTempProbeNo() + "离线，温度数据缺失")
                    .build();
            evidenceGapRepository.save(gap);
        }

        if (probeStatus == ProbeStatus.ONLINE) {
            evidenceGapRepository
                    .findFirstByDistributionOrderIdAndProbeNoAndStatusOrderByOfflineAtDesc(
                            request.getDistributionOrderId(),
                            order.getTempProbeNo(),
                            EvidenceGapStatus.OPEN)
                    .ifPresent(gap -> {
                        gap.setBackOnlineAt(now);
                        gap.setDuration(Duration.between(gap.getOfflineAt(), now).toMinutes());
                        gap.setStatus(EvidenceGapStatus.CLOSED);
                        gap.setDescription("探头" + gap.getProbeNo()
                                + "离线" + gap.getDuration() + "分钟，温度数据缺失");
                        evidenceGapRepository.save(gap);
                    });
        }

        return report;
    }

    public List<TemperatureEvidenceGap> getEvidenceGaps() {
        return evidenceGapRepository.findAll();
    }

    public List<TemperatureEvidenceGap> getEvidenceGapsByOrderId(String orderId) {
        Long id = Long.valueOf(orderId);
        return evidenceGapRepository.findByDistributionOrderId(id);
    }
}
