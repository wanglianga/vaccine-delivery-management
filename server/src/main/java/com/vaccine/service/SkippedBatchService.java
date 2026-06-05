package com.vaccine.service;

import com.vaccine.dto.ReviewSkippedBatchRequest;
import com.vaccine.dto.SkipBatchRequest;
import com.vaccine.entity.BatchAdjustmentRecord;
import com.vaccine.entity.DistributionRecommendation;
import com.vaccine.entity.SkippedBatchReview;
import com.vaccine.entity.VaccineBatch;
import com.vaccine.enums.BatchStatus;
import com.vaccine.exception.BusinessException;
import com.vaccine.repository.BatchAdjustmentRecordRepository;
import com.vaccine.repository.DistributionRecommendationRepository;
import com.vaccine.repository.SkippedBatchReviewRepository;
import com.vaccine.repository.VaccineBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SkippedBatchService {

    @Autowired
    private SkippedBatchReviewRepository reviewRepository;

    @Autowired
    private BatchAdjustmentRecordRepository adjustmentRecordRepository;

    @Autowired
    private VaccineBatchRepository vaccineBatchRepository;

    @Autowired
    private DistributionRecommendationRepository recommendationRepository;

    @Autowired
    private DistributionRecommendationService recommendationService;

    public List<SkippedBatchReview> getPendingReviews() {
        return reviewRepository.findByReviewedFalse();
    }

    public List<SkippedBatchReview> getReviewsByBatchId(Long batchId) {
        return reviewRepository.findByBatchIdOrderByCreatedAtDesc(batchId);
    }

    public List<BatchAdjustmentRecord> getAdjustmentRecords(Long batchId) {
        return adjustmentRecordRepository.findByBatchIdOrderByCreatedAtDesc(batchId);
    }

    @Transactional
    public SkippedBatchReview skipNearExpiryBatch(SkipBatchRequest request) {
        VaccineBatch skippedBatch = vaccineBatchRepository.findById(request.getSkippedBatchId())
                .orElseThrow(() -> new BusinessException("被跳过的批次不存在，ID: " + request.getSkippedBatchId()));

        VaccineBatch selectedBatch = vaccineBatchRepository.findById(request.getSelectedBatchId())
                .orElseThrow(() -> new BusinessException("选择的批次不存在，ID: " + request.getSelectedBatchId()));

        if (!recommendationService.isNearExpiry(skippedBatch)) {
            throw new BusinessException("被跳过的批次不是临期批次，无需特殊处理");
        }

        if (recommendationService.isNearExpiry(selectedBatch) && 
            skippedBatch.getExpiryDate().isAfter(selectedBatch.getExpiryDate())) {
            throw new BusinessException("选择的批次比被跳过的批次更临期，请重新选择");
        }

        if (request.getSkipReason() == null || request.getSkipReason().trim().isEmpty()) {
            throw new BusinessException("跳过临期批次必须填写原因");
        }

        BatchStatus targetStatus;
        try {
            targetStatus = BatchStatus.valueOf(request.getTargetStatus().toUpperCase());
        } catch (Exception e) {
            throw new BusinessException("无效的目标状态: " + request.getTargetStatus());
        }

        if (targetStatus != BatchStatus.PENDING_ARRANGEMENT && 
            targetStatus != BatchStatus.PENDING_REPORT_LOSS && 
            targetStatus != BatchStatus.PENDING_TRANSFER) {
            throw new BusinessException("被跳过的临期批次只能进入待安排、待报损或待转配状态");
        }

        DistributionRecommendation skippedRec = recommendationRepository.findAll().stream()
                .filter(r -> r.getBatchId().equals(skippedBatch.getId()))
                .findFirst()
                .orElse(null);

        DistributionRecommendation selectedRec = recommendationRepository.findAll().stream()
                .filter(r -> r.getBatchId().equals(selectedBatch.getId()))
                .findFirst()
                .orElse(null);

        int skippedOrder = skippedRec != null ? skippedRec.getRecommendedOrder() : 1;
        int selectedOrder = selectedRec != null ? selectedRec.getRecommendedOrder() : 2;

        BatchAdjustmentRecord adjustmentRecord = BatchAdjustmentRecord.builder()
                .batchId(skippedBatch.getId())
                .batchNo(skippedBatch.getBatchNo())
                .recommendedOrder(skippedOrder)
                .actualOrder(selectedOrder)
                .skipReason(request.getSkipReason())
                .adjustedBy(request.getSkippedBy())
                .affectedClinics(request.getAffectedClinics())
                .createdAt(LocalDateTime.now())
                .build();
        adjustmentRecordRepository.save(adjustmentRecord);

        SkippedBatchReview review = SkippedBatchReview.builder()
                .batchId(skippedBatch.getId())
                .batchNo(skippedBatch.getBatchNo())
                .vaccineName(skippedBatch.getVaccineName())
                .skipReason(request.getSkipReason())
                .skippedBy(request.getSkippedBy())
                .skippedAt(LocalDateTime.now())
                .targetStatus(targetStatus)
                .reviewed(false)
                .createdAt(LocalDateTime.now())
                .build();
        review = reviewRepository.save(review);

        skippedBatch.setStatus(BatchStatus.UNDER_REVIEW);
        skippedBatch.setLastAdjustedBy(request.getSkippedBy());
        skippedBatch.setLastAdjustedAt(LocalDateTime.now());
        skippedBatch.setAffectedClinics(request.getAffectedClinics());
        skippedBatch.setUpdatedAt(LocalDateTime.now());
        vaccineBatchRepository.save(skippedBatch);

        return review;
    }

    @Transactional
    public SkippedBatchReview reviewSkippedBatch(Long reviewId, ReviewSkippedBatchRequest request) {
        SkippedBatchReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("复核记录不存在，ID: " + reviewId));

        if (review.getReviewed()) {
            throw new BusinessException("该记录已复核，不能重复复核");
        }

        VaccineBatch batch = vaccineBatchRepository.findById(review.getBatchId())
                .orElseThrow(() -> new BusinessException("疫苗批次不存在，ID: " + review.getBatchId()));

        review.setReviewComment(request.getReviewComment());
        review.setReviewedBy(request.getReviewedBy());
        review.setReviewedAt(LocalDateTime.now());
        review.setReviewed(true);
        review.setUpdatedAt(LocalDateTime.now());

        if (request.getApproved()) {
            batch.setStatus(review.getTargetStatus());
        } else {
            batch.setStatus(BatchStatus.AVAILABLE);
        }
        batch.setUpdatedAt(LocalDateTime.now());

        vaccineBatchRepository.save(batch);
        return reviewRepository.save(review);
    }

    @Transactional
    public VaccineBatch updateBatchStatus(Long batchId, String status, String operator, String remark) {
        VaccineBatch batch = vaccineBatchRepository.findById(batchId)
                .orElseThrow(() -> new BusinessException("疫苗批次不存在，ID: " + batchId));

        BatchStatus newStatus;
        try {
            newStatus = BatchStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new BusinessException("无效的状态: " + status);
        }

        if (batch.getStatus() == newStatus) {
            return batch;
        }

        batch.setStatus(newStatus);
        batch.setLastAdjustedBy(operator);
        batch.setLastAdjustedAt(LocalDateTime.now());
        batch.setUpdatedAt(LocalDateTime.now());

        return vaccineBatchRepository.save(batch);
    }
}
