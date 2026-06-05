package com.vaccine.config;

import com.vaccine.entity.*;
import com.vaccine.enums.*;
import com.vaccine.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final VaccineBatchRepository batchRepository;
    private final DistributionOrderRepository orderRepository;
    private final TransitReportRepository transitRepository;
    private final AcceptanceRecordRepository acceptanceRepository;
    private final TemperatureEvidenceGapRepository gapRepository;
    private final ReturnTaskRepository returnTaskRepository;
    private final ClinicInventoryRepository inventoryRepository;

    public DataInitializer(VaccineBatchRepository batchRepository,
                           DistributionOrderRepository orderRepository,
                           TransitReportRepository transitRepository,
                           AcceptanceRecordRepository acceptanceRepository,
                           TemperatureEvidenceGapRepository gapRepository,
                           ReturnTaskRepository returnTaskRepository,
                           ClinicInventoryRepository inventoryRepository) {
        this.batchRepository = batchRepository;
        this.orderRepository = orderRepository;
        this.transitRepository = transitRepository;
        this.acceptanceRepository = acceptanceRepository;
        this.gapRepository = gapRepository;
        this.returnTaskRepository = returnTaskRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) {
        if (batchRepository.count() > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        VaccineBatch b1 = batchRepository.save(VaccineBatch.builder()
                .batchNo("20240601-001").vaccineName("新冠疫苗（Vero细胞）")
                .manufacturer("北京科兴中维").specification("0.5ml/支")
                .expiryDate(LocalDate.of(2025, 6, 1)).storageTempZone("2-8°C")
                .batchReleaseDoc("BR-2024-0601-001.pdf")
                .distributableQty(5000).totalQty(10000)
                .status(BatchStatus.AVAILABLE)
                .createdAt(now.minusDays(30)).updatedAt(now.minusDays(30)).build());

        VaccineBatch b2 = batchRepository.save(VaccineBatch.builder()
                .batchNo("20240615-002").vaccineName("乙肝疫苗")
                .manufacturer("深圳康泰").specification("10μg/0.5ml")
                .expiryDate(LocalDate.of(2025, 12, 15)).storageTempZone("2-8°C")
                .batchReleaseDoc("BR-2024-0615-002.pdf")
                .distributableQty(2000).totalQty(8000)
                .status(BatchStatus.PARTIAL)
                .createdAt(now.minusDays(15)).updatedAt(now.minusDays(5)).build());

        VaccineBatch b3 = batchRepository.save(VaccineBatch.builder()
                .batchNo("20240701-003").vaccineName("流感疫苗（四价）")
                .manufacturer("华兰生物").specification("0.5ml/支")
                .expiryDate(LocalDate.of(2025, 7, 1)).storageTempZone("2-8°C")
                .batchReleaseDoc("BR-2024-0701-003.pdf")
                .distributableQty(3000).totalQty(3000)
                .status(BatchStatus.AVAILABLE)
                .createdAt(now.minusDays(2)).updatedAt(now.minusDays(2)).build());

        VaccineBatch b4 = batchRepository.save(VaccineBatch.builder()
                .batchNo("20240801-006").vaccineName("新冠疫苗（mRNA）")
                .manufacturer("石药集团").specification("0.3ml/支")
                .expiryDate(LocalDate.of(2025, 8, 1)).storageTempZone("-20°C")
                .batchReleaseDoc("BR-2024-0801-006.pdf")
                .distributableQty(1500).totalQty(4000)
                .status(BatchStatus.AVAILABLE)
                .createdAt(now.minusDays(1)).updatedAt(now.minusDays(1)).build());

        DistributionOrder d1 = orderRepository.save(DistributionOrder.builder()
                .orderNo("DIS-20240701-001").batchId(b1.getId()).batchNo(b1.getBatchNo())
                .vaccineName(b1.getVaccineName()).quantity(500)
                .coldChainBoxNo("CCB-001").tempProbeNo("TP-001").sealNo("SL-20240701-001")
                .vehicleNo("京A·12345").driverName("张伟").driverPhone("13800138001")
                .estimatedArrivalTime(now.plusHours(6))
                .targetClinic("朝阳区劲松社区卫生服务中心")
                .status(OrderStatus.IN_TRANSIT)
                .createdAt(now.minusHours(4)).updatedAt(now.minusHours(2)).build());

        DistributionOrder d2 = orderRepository.save(DistributionOrder.builder()
                .orderNo("DIS-20240702-002").batchId(b2.getId()).batchNo(b2.getBatchNo())
                .vaccineName(b2.getVaccineName()).quantity(300)
                .coldChainBoxNo("CCB-002").tempProbeNo("TP-002").sealNo("SL-20240702-002")
                .vehicleNo("京B·67890").driverName("李强").driverPhone("13900139002")
                .estimatedArrivalTime(now.plusHours(4))
                .targetClinic("海淀区中关村社区卫生服务中心")
                .status(OrderStatus.IN_TRANSIT)
                .createdAt(now.minusHours(3)).updatedAt(now.minusHours(1)).build());

        DistributionOrder d3 = orderRepository.save(DistributionOrder.builder()
                .orderNo("DIS-20240703-003").batchId(b3.getId()).batchNo(b3.getBatchNo())
                .vaccineName(b3.getVaccineName()).quantity(200)
                .coldChainBoxNo("CCB-003").tempProbeNo("TP-003").sealNo("SL-20240703-003")
                .vehicleNo("京C·11111").driverName("王磊").driverPhone("13700137003")
                .estimatedArrivalTime(now.plusHours(8))
                .targetClinic("西城区月坛社区卫生服务中心")
                .status(OrderStatus.PENDING)
                .createdAt(now.minusHours(1)).updatedAt(now.minusHours(1)).build());

        DistributionOrder d4 = orderRepository.save(DistributionOrder.builder()
                .orderNo("DIS-20240628-004").batchId(b1.getId()).batchNo(b1.getBatchNo())
                .vaccineName(b1.getVaccineName()).quantity(1000)
                .coldChainBoxNo("CCB-004").tempProbeNo("TP-004").sealNo("SL-20240628-004")
                .vehicleNo("京D·22222").driverName("赵鹏").driverPhone("13600136004")
                .estimatedArrivalTime(now.minusHours(20))
                .targetClinic("东城区和平里社区卫生服务中心")
                .status(OrderStatus.ACCEPTED)
                .createdAt(now.minusDays(3)).updatedAt(now.minusDays(2)).build());

        DistributionOrder d5 = orderRepository.save(DistributionOrder.builder()
                .orderNo("DIS-20240625-005").batchId(b2.getId()).batchNo(b2.getBatchNo())
                .vaccineName(b2.getVaccineName()).quantity(400)
                .coldChainBoxNo("CCB-005").tempProbeNo("TP-005").sealNo("SL-20240625-005")
                .vehicleNo("京E·33333").driverName("刘洋").driverPhone("13500135005")
                .estimatedArrivalTime(now.minusDays(5))
                .targetClinic("丰台区方庄社区卫生服务中心")
                .status(OrderStatus.REJECTED)
                .createdAt(now.minusDays(6)).updatedAt(now.minusDays(5)).build());

        transitRepository.save(TransitReport.builder()
                .distributionOrderId(d1.getId()).orderNo(d1.getOrderNo()).reportTime(now.minusHours(2))
                .temperature(4.2).latitude(39.9042).longitude(116.4074)
                .locationDesc("北京市朝阳区国贸桥")
                .probeStatus(ProbeStatus.ONLINE).probeBatteryLevel(85).build());

        transitRepository.save(TransitReport.builder()
                .distributionOrderId(d1.getId()).orderNo(d1.getOrderNo()).reportTime(now.minusHours(1))
                .temperature(5.1).latitude(39.9142).longitude(116.4174)
                .locationDesc("北京市朝阳区大望路")
                .stopPoint("停留15分钟 - 等待红灯")
                .probeStatus(ProbeStatus.ONLINE).probeBatteryLevel(83).build());

        transitRepository.save(TransitReport.builder()
                .distributionOrderId(d2.getId()).orderNo(d2.getOrderNo()).reportTime(now.minusHours(1))
                .temperature(3.8).latitude(39.9842).longitude(116.3074)
                .locationDesc("北京市海淀区中关村大街")
                .probeStatus(ProbeStatus.ONLINE).probeBatteryLevel(92).build());

        transitRepository.save(TransitReport.builder()
                .distributionOrderId(d2.getId()).orderNo(d2.getOrderNo()).reportTime(now.minusMinutes(30))
                .temperature(-1.2).latitude(39.9942).longitude(116.3174)
                .locationDesc("北京市海淀区五道口")
                .stopPoint("停留30分钟 - 卸货等待")
                .boxOpenRecord("开箱检查 - 取出部分疫苗")
                .probeStatus(ProbeStatus.OFFLINE).probeBatteryLevel(12).build());

        transitRepository.save(TransitReport.builder()
                .distributionOrderId(d2.getId()).orderNo(d2.getOrderNo()).reportTime(now.minusMinutes(10))
                .temperature(4.5).latitude(40.0042).longitude(116.3274)
                .locationDesc("北京市海淀区清华园")
                .probeStatus(ProbeStatus.LOW_BATTERY).probeBatteryLevel(8).build());

        acceptanceRepository.save(AcceptanceRecord.builder()
                .distributionOrderId(d4.getId()).orderNo(d4.getOrderNo())
                .batchNo(d4.getBatchNo()).vaccineName(d4.getVaccineName())
                .sentQty(1000).receivedQty(1000)
                .sealIntact(true).tempCurveOk(true)
                .arrivalTime(now.minusDays(2))
                .status(AcceptanceStatus.ACCEPTED)
                .createdAt(now.minusDays(2)).updatedAt(now.minusDays(2)).build());

        AcceptanceRecord rejectedRecord = acceptanceRepository.save(AcceptanceRecord.builder()
                .distributionOrderId(d5.getId()).orderNo(d5.getOrderNo())
                .batchNo(d5.getBatchNo()).vaccineName(d5.getVaccineName())
                .sentQty(400).receivedQty(400)
                .sealIntact(false).tempCurveOk(true)
                .arrivalTime(now.minusDays(5))
                .status(AcceptanceStatus.REJECTED)
                .rejectionReason("封签破损，无法确认运输过程中是否被打开")
                .createdAt(now.minusDays(5)).updatedAt(now.minusDays(5)).build());

        ReturnTask rt = returnTaskRepository.save(ReturnTask.builder()
                .acceptanceRecordId(rejectedRecord.getId())
                .orderNo(rejectedRecord.getOrderNo())
                .reason("封签破损，门诊拒收")
                .status(ReturnTaskStatus.IN_RETURN)
                .createdAt(now.minusDays(5)).updatedAt(now.minusDays(4)).build());
        rejectedRecord.setReturnTaskId(rt.getId());
        acceptanceRepository.save(rejectedRecord);

        acceptanceRepository.save(AcceptanceRecord.builder()
                .distributionOrderId(d1.getId()).orderNo(d1.getOrderNo())
                .batchNo(d1.getBatchNo()).vaccineName(d1.getVaccineName())
                .sentQty(500).receivedQty(488)
                .sealIntact(true).tempCurveOk(true)
                .status(AcceptanceStatus.QTY_MISMATCH)
                .createdAt(now.minusHours(1)).updatedAt(now.minusHours(1)).build());

        acceptanceRepository.save(AcceptanceRecord.builder()
                .distributionOrderId(d2.getId()).orderNo(d2.getOrderNo())
                .batchNo(d2.getBatchNo()).vaccineName(d2.getVaccineName())
                .sentQty(300).receivedQty(300)
                .sealIntact(true).tempCurveOk(false)
                .status(AcceptanceStatus.EVIDENCE_GAP)
                .createdAt(now.minusMinutes(30)).updatedAt(now.minusMinutes(30)).build());

        gapRepository.save(TemperatureEvidenceGap.builder()
                .distributionOrderId(d2.getId()).orderNo(d2.getOrderNo())
                .probeNo("TP-002")
                .offlineAt(now.minusMinutes(30))
                .backOnlineAt(now.minusMinutes(10))
                .duration(20L)
                .status(EvidenceGapStatus.ACKNOWLEDGED)
                .description("探头TP-002在五道口区域离线20分钟，期间温度数据缺失").build());

        gapRepository.save(TemperatureEvidenceGap.builder()
                .distributionOrderId(d1.getId()).orderNo(d1.getOrderNo())
                .probeNo("TP-001")
                .offlineAt(now.minusMinutes(5))
                .status(EvidenceGapStatus.OPEN)
                .description("探头TP-001在劲松区域离线，尚未恢复").build());

        inventoryRepository.save(ClinicInventory.builder()
                .batchId(b1.getId()).batchNo(b1.getBatchNo())
                .vaccineName(b1.getVaccineName()).quantity(1000)
                .storageTempZone(b1.getStorageTempZone())
                .expiryDate(b1.getExpiryDate())
                .clinicName("东城区和平里社区卫生服务中心")
                .receivedAt(now.minusDays(2)).build());
    }
}
