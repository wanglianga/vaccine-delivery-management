package com.vaccine.repository;

import com.vaccine.entity.SkippedBatchReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkippedBatchReviewRepository extends JpaRepository<SkippedBatchReview, Long> {
    List<SkippedBatchReview> findByReviewedFalse();
    List<SkippedBatchReview> findByBatchIdOrderByCreatedAtDesc(Long batchId);
}
