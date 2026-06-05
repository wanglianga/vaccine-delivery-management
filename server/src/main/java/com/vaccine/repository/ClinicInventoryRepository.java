package com.vaccine.repository;

import com.vaccine.entity.ClinicInventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicInventoryRepository extends JpaRepository<ClinicInventory, Long> {
}
