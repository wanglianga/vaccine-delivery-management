package com.vaccine.service;

import com.vaccine.dto.GenerateRecommendationRequest;
import com.vaccine.entity.DistributionRecommendation;
import com.vaccine.entity.VaccineBatch;
import com.vaccine.enums.BatchStatus;
import com.vaccine.exception.BusinessException;
import com.vaccine.repository.DistributionRecommendationRepository;
import com.vaccine.repository.VaccineBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DistributionRecommendationService {

    private static final double EXPIRY_WEIGHT = 0.35;
    private static final double APPOINTMENT_WEIGHT = 0.25;
    private static final double INVENTORY_WEIGHT = 0.15;
    private static final double DISTANCE_WEIGHT = 0.15;
    private static final double CAPACITY_WEIGHT = 0.10;

    private static final long NEAR_EXPIRY_THRESHOLD_DAYS = 90;

    @Autowired
    private DistributionRecommendationRepository recommendationRepository;

    @Autowired
    private VaccineBatchRepository vaccineBatchRepository;

    public List<DistributionRecommendation> getAllRecommendations() {
        return recommendationRepository.findAllByOrderByRecommendedOrderAsc();
    }

    public List<DistributionRecommendation> getRecommendationsByVaccine(String vaccineName) {
        return recommendationRepository.findByVaccineNameOrderByRecommendedOrderAsc(vaccineName);
    }

    @Transactional
    public List<DistributionRecommendation> generateRecommendations(GenerateRecommendationRequest request) {
        recommendationRepository.deleteAll();

        List<VaccineBatch> batches;
        if (request.getBatchIds() != null && !request.getBatchIds().isEmpty()) {
            batches = vaccineBatchRepository.findAllById(request.getBatchIds());
        } else if (request.getVaccineName() != null && !request.getVaccineName().isEmpty()) {
            batches = vaccineBatchRepository.findAll().stream()
                    .filter(b -> b.getVaccineName().equals(request.getVaccineName()))
                    .filter(b -> b.getStatus() == BatchStatus.AVAILABLE || b.getStatus() == BatchStatus.PARTIAL)
                    .collect(Collectors.toList());
        } else {
            batches = vaccineBatchRepository.findAll().stream()
                    .filter(b -> b.getStatus() == BatchStatus.AVAILABLE || b.getStatus() == BatchStatus.PARTIAL)
                    .collect(Collectors.toList());
        }

        if (batches.isEmpty()) {
            throw new BusinessException("没有可配送的疫苗批次");
        }

        List<DistributionRecommendation> recommendations = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < batches.size(); i++) {
            VaccineBatch batch = batches.get(i);
            long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpiryDate());

            double expiryScore = calculateExpiryScore(daysToExpiry);
            double appointmentScore = calculateAppointmentScore(batch, request);
            double inventoryScore = calculateInventoryScore(batch);
            double distanceScore = calculateDistanceScore(request.getDistanceKm());
            double capacityScore = calculateCapacityScore(request.getColdChainBoxCapacity(), request.getColdChainBoxUsed(), batch.getDistributableQty());

            double totalScore = expiryScore * EXPIRY_WEIGHT
                    + appointmentScore * APPOINTMENT_WEIGHT
                    + inventoryScore * INVENTORY_WEIGHT
                    + distanceScore * DISTANCE_WEIGHT
                    + capacityScore * CAPACITY_WEIGHT;

            DistributionRecommendation recommendation = DistributionRecommendation.builder()
                    .batchId(batch.getId())
                    .batchNo(batch.getBatchNo())
                    .vaccineName(batch.getVaccineName())
                    .recommendedOrder(0)
                    .expiryScore(expiryScore)
                    .appointmentScore(appointmentScore)
                    .inventoryScore(inventoryScore)
                    .distanceScore(distanceScore)
                    .capacityScore(capacityScore)
                    .totalScore(totalScore)
                    .daysToExpiry(daysToExpiry)
                    .appointmentCount((int)(Math.random() * 100 + 10))
                    .targetClinic(request.getTargetClinic())
                    .distanceKm(request.getDistanceKm())
                    .coldChainBoxRemaining(request.getColdChainBoxCapacity() != null && request.getColdChainBoxUsed() != null
                            ? request.getColdChainBoxCapacity() - request.getColdChainBoxUsed() : null)
                    .createdAt(now)
                    .createdBy(request.getOperator())
                    .build();

            recommendations.add(recommendation);
        }

        recommendations.sort(Comparator.comparingDouble(DistributionRecommendation::getTotalScore).reversed());

        for (int i = 0; i < recommendations.size(); i++) {
            recommendations.get(i).setRecommendedOrder(i + 1);
        }

        List<DistributionRecommendation> saved = recommendationRepository.saveAll(recommendations);

        for (DistributionRecommendation rec : saved) {
            VaccineBatch batch = vaccineBatchRepository.findById(rec.getBatchId()).orElse(null);
            if (batch != null) {
                batch.setRecommendedOrder(rec.getRecommendedOrder());
                batch.setUpdatedAt(LocalDateTime.now());
                vaccineBatchRepository.save(batch);
            }
        }

        return saved;
    }

    private double calculateExpiryScore(long daysToExpiry) {
        if (daysToExpiry <= 0) return 1.0;
        if (daysToExpiry <= 30) return 0.95;
        if (daysToExpiry <= 60) return 0.85;
        if (daysToExpiry <= 90) return 0.70;
        if (daysToExpiry <= 180) return 0.50;
        if (daysToExpiry <= 365) return 0.30;
        return 0.10;
    }

    private double calculateAppointmentScore(VaccineBatch batch, GenerateRecommendationRequest request) {
        if (request.getTargetClinic() == null || request.getTargetClinic().isEmpty()) {
            return 0.5;
        }
        int appointmentCount = (int)(Math.random() * 100 + 10);
        return Math.min(1.0, appointmentCount / 100.0);
    }

    private double calculateInventoryScore(VaccineBatch batch) {
        double ratio = (double) batch.getDistributableQty() / batch.getTotalQty();
        if (ratio <= 0.1) return 1.0;
        if (ratio <= 0.3) return 0.8;
        if (ratio <= 0.5) return 0.6;
        if (ratio <= 0.7) return 0.4;
        return 0.2;
    }

    private double calculateDistanceScore(Double distanceKm) {
        if (distanceKm == null) return 0.5;
        if (distanceKm <= 10) return 1.0;
        if (distanceKm <= 30) return 0.8;
        if (distanceKm <= 50) return 0.6;
        if (distanceKm <= 100) return 0.4;
        return 0.2;
    }

    private double calculateCapacityScore(Integer capacity, Integer used, Integer batchQty) {
        if (capacity == null || used == null || batchQty == null) return 0.5;
        int remaining = capacity - used;
        if (remaining >= batchQty) return 1.0;
        if (remaining >= batchQty * 0.7) return 0.8;
        if (remaining >= batchQty * 0.5) return 0.6;
        if (remaining >= batchQty * 0.3) return 0.4;
        return 0.2;
    }

    public boolean isNearExpiry(VaccineBatch batch) {
        long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpiryDate());
        return daysToExpiry <= NEAR_EXPIRY_THRESHOLD_DAYS;
    }

    public long getDaysToExpiry(VaccineBatch batch) {
        return ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpiryDate());
    }
}
