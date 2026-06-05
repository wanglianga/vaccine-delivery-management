package com.vaccine.service;

import com.vaccine.dto.CreateBatchRequest;
import com.vaccine.entity.VaccineBatch;
import com.vaccine.enums.BatchStatus;
import com.vaccine.exception.BusinessException;
import com.vaccine.repository.VaccineBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class VaccineBatchService {

    @Autowired
    private VaccineBatchRepository vaccineBatchRepository;

    public List<VaccineBatch> getAllBatches() {
        return vaccineBatchRepository.findAll();
    }

    public VaccineBatch getBatchById(Long id) {
        return vaccineBatchRepository.findById(id)
                .orElseThrow(() -> new BusinessException("疫苗批次不存在，ID: " + id));
    }

    public VaccineBatch createBatch(CreateBatchRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate expiryDate = LocalDate.parse(request.getExpiryDate(), DateTimeFormatter.ISO_LOCAL_DATE);

        VaccineBatch batch = VaccineBatch.builder()
                .batchNo(request.getBatchNo())
                .vaccineName(request.getVaccineName())
                .manufacturer(request.getManufacturer())
                .specification(request.getSpecification())
                .expiryDate(expiryDate)
                .storageTempZone(request.getStorageTempZone())
                .batchReleaseDoc(request.getBatchReleaseDoc())
                .totalQty(request.getTotalQty())
                .distributableQty(request.getDistributableQty())
                .status(BatchStatus.AVAILABLE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return vaccineBatchRepository.save(batch);
    }
}
