package com.vaccine.repository;

import com.vaccine.entity.VaccineBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaccineBatchRepository extends JpaRepository<VaccineBatch, Long> {
}
