package com.vaccine.controller;

import com.vaccine.dto.CreateBatchRequest;
import com.vaccine.entity.VaccineBatch;
import com.vaccine.service.VaccineBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class VaccineBatchController {

    @Autowired
    private VaccineBatchService vaccineBatchService;

    @GetMapping("/batches")
    public ResponseEntity<List<VaccineBatch>> getAllBatches() {
        return ResponseEntity.ok(vaccineBatchService.getAllBatches());
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<VaccineBatch> getBatchById(@PathVariable Long id) {
        return ResponseEntity.ok(vaccineBatchService.getBatchById(id));
    }

    @PostMapping("/batches")
    public ResponseEntity<VaccineBatch> createBatch(@RequestBody CreateBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vaccineBatchService.createBatch(request));
    }
}
