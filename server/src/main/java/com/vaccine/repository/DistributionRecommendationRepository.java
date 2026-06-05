package com.vaccine.repository;

import com.vaccine.entity.DistributionRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistributionRecommendationRepository extends JpaRepository<DistributionRecommendation, Long> {
    List<DistributionRecommendation> findAllByOrderByRecommendedOrderAsc();
    List<DistributionRecommendation> findByVaccineNameOrderByRecommendedOrderAsc(String vaccineName);
}
