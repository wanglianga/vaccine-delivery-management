package com.vaccine.controller;

import com.vaccine.dto.TransitReportRequest;
import com.vaccine.entity.TemperatureEvidenceGap;
import com.vaccine.entity.TransitReport;
import com.vaccine.service.TransitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TransitController {

    @Autowired
    private TransitService transitService;

    @GetMapping("/transit-reports")
    public ResponseEntity<List<TransitReport>> getReports(
            @RequestParam(required = false) String orderId) {
        if (orderId != null) {
            return ResponseEntity.ok(transitService.getReportsByOrderId(orderId));
        }
        return ResponseEntity.ok(transitService.getAllReports());
    }

    @PostMapping("/transit-reports")
    public ResponseEntity<TransitReport> submitReport(@RequestBody TransitReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transitService.submitReport(request));
    }

    @GetMapping("/evidence-gaps")
    public ResponseEntity<List<TemperatureEvidenceGap>> getEvidenceGaps(
            @RequestParam(required = false) String orderId) {
        if (orderId != null) {
            return ResponseEntity.ok(transitService.getEvidenceGapsByOrderId(orderId));
        }
        return ResponseEntity.ok(transitService.getEvidenceGaps());
    }
}
