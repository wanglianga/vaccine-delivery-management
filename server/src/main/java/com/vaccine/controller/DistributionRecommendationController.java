package com.vaccine.controller;

import com.vaccine.dto.GenerateRecommendationRequest;
import com.vaccine.dto.ReviewSkippedBatchRequest;
import com.vaccine.dto.SkipBatchRequest;
import com.vaccine.dto.UpdateBatchStatusRequest;
import com.vaccine.entity.BatchAdjustmentRecord;
import com.vaccine.entity.DistributionRecommendation;
import com.vaccine.entity.SkippedBatchReview;
import com.vaccine.entity.VaccineBatch;
import com.vaccine.service.DistributionRecommendationService;
import com.vaccine.service.SkippedBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DistributionRecommendationController {

    @Autowired
    private DistributionRecommendationService recommendationService;

    @Autowired
    private SkippedBatchService skippedBatchService;

    @GetMapping("/distribution-recommendations")
    public ResponseEntity<List<DistributionRecommendation>> getRecommendations(
            @RequestParam(required = false) String vaccineName) {
        if (vaccineName != null && !vaccineName.isEmpty()) {
            return ResponseEntity.ok(recommendationService.getRecommendationsByVaccine(vaccineName));
        }
        return ResponseEntity.ok(recommendationService.getAllRecommendations());
    }

    @PostMapping("/distribution-recommendations/generate")
    public ResponseEntity<List<DistributionRecommendation>> generateRecommendations(
            @RequestBody GenerateRecommendationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recommendationService.generateRecommendations(request));
    }

    @GetMapping("/skipped-batch-reviews/pending")
    public ResponseEntity<List<SkippedBatchReview>> getPendingReviews() {
        return ResponseEntity.ok(skippedBatchService.getPendingReviews());
    }

    @GetMapping("/skipped-batch-reviews/batch/{batchId}")
    public ResponseEntity<List<SkippedBatchReview>> getReviewsByBatchId(@PathVariable Long batchId) {
        return ResponseEntity.ok(skippedBatchService.getReviewsByBatchId(batchId));
    }

    @PostMapping("/skipped-batch-reviews")
    public ResponseEntity<SkippedBatchReview> skipNearExpiryBatch(@RequestBody SkipBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(skippedBatchService.skipNearExpiryBatch(request));
    }

    @PutMapping("/skipped-batch-reviews/{reviewId}/review")
    public ResponseEntity<SkippedBatchReview> reviewSkippedBatch(
            @PathVariable Long reviewId,
            @RequestBody ReviewSkippedBatchRequest request) {
        return ResponseEntity.ok(skippedBatchService.reviewSkippedBatch(reviewId, request));
    }

    @GetMapping("/batch-adjustment-records/batch/{batchId}")
    public ResponseEntity<List<BatchAdjustmentRecord>> getAdjustmentRecords(@PathVariable Long batchId) {
        return ResponseEntity.ok(skippedBatchService.getAdjustmentRecords(batchId));
    }

    @PutMapping("/batches/{batchId}/status")
    public ResponseEntity<VaccineBatch> updateBatchStatus(
            @PathVariable Long batchId,
            @RequestBody UpdateBatchStatusRequest request) {
        return ResponseEntity.ok(skippedBatchService.updateBatchStatus(
                batchId, request.getStatus(), request.getOperator(), request.getRemark()));
    }
}
