package com.vaccine.controller;

import com.vaccine.dto.AcceptDeliveryRequest;
import com.vaccine.dto.ConfirmQuantityRequest;
import com.vaccine.dto.RejectDeliveryRequest;
import com.vaccine.entity.AcceptanceRecord;
import com.vaccine.entity.ClinicInventory;
import com.vaccine.entity.QuantityConfirmation;
import com.vaccine.entity.ReturnTask;
import com.vaccine.service.AcceptanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AcceptanceController {

    @Autowired
    private AcceptanceService acceptanceService;

    @GetMapping("/acceptance-records")
    public ResponseEntity<List<AcceptanceRecord>> getAllRecords() {
        return ResponseEntity.ok(acceptanceService.getAllRecords());
    }

    @PutMapping("/acceptance-records/{id}/accept")
    public ResponseEntity<AcceptanceRecord> acceptDelivery(
            @PathVariable Long id,
            @RequestBody AcceptDeliveryRequest request) {
        return ResponseEntity.ok(acceptanceService.acceptDelivery(id, request));
    }

    @PutMapping("/acceptance-records/{id}/reject")
    public ResponseEntity<AcceptanceRecord> rejectDelivery(
            @PathVariable Long id,
            @RequestBody RejectDeliveryRequest request) {
        return ResponseEntity.ok(acceptanceService.rejectDelivery(id, request));
    }

    @PostMapping("/acceptance-records/{id}/confirm-quantity")
    public ResponseEntity<QuantityConfirmation> confirmQuantity(
            @PathVariable Long id,
            @RequestBody ConfirmQuantityRequest request) {
        return ResponseEntity.ok(acceptanceService.confirmQuantity(id, request));
    }

    @PutMapping("/acceptance-records/{id}/acknowledge-gap")
    public ResponseEntity<AcceptanceRecord> acknowledgeGap(@PathVariable Long id) {
        return ResponseEntity.ok(acceptanceService.acknowledgeGap(id));
    }

    @PutMapping("/acceptance-records/{id}/resume-inbound")
    public ResponseEntity<AcceptanceRecord> resumeInbound(@PathVariable Long id) {
        return ResponseEntity.ok(acceptanceService.resumeInbound(id));
    }

    @GetMapping("/return-tasks")
    public ResponseEntity<List<ReturnTask>> getReturnTasks() {
        return ResponseEntity.ok(acceptanceService.getReturnTasks());
    }

    @GetMapping("/clinic-inventory")
    public ResponseEntity<List<ClinicInventory>> getClinicInventory() {
        return ResponseEntity.ok(acceptanceService.getClinicInventory());
    }

    @GetMapping("/quantity-confirmations")
    public ResponseEntity<List<QuantityConfirmation>> getQuantityConfirmations() {
        return ResponseEntity.ok(acceptanceService.getQuantityConfirmations());
    }
}
