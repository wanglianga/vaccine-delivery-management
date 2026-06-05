package com.vaccine.controller;

import com.vaccine.dto.*;
import com.vaccine.entity.BoxAcceptanceRecord;
import com.vaccine.entity.ColdChainBox;
import com.vaccine.entity.TransitSegment;
import com.vaccine.service.ColdChainBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ColdChainBoxController {

    @Autowired
    private ColdChainBoxService coldChainBoxService;

    @GetMapping("/cold-chain-boxes")
    public ResponseEntity<List<ColdChainBox>> getBoxes(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Long orderId) {
        if (batchId != null) {
            return ResponseEntity.ok(coldChainBoxService.getBoxesByBatchId(batchId));
        }
        if (orderId != null) {
            return ResponseEntity.ok(coldChainBoxService.getBoxesByOrderId(orderId));
        }
        return ResponseEntity.ok(coldChainBoxService.getAllBoxes());
    }

    @GetMapping("/cold-chain-boxes/{id}")
    public ResponseEntity<ColdChainBox> getBoxById(@PathVariable Long id) {
        return ResponseEntity.ok(coldChainBoxService.getBoxById(id));
    }

    @GetMapping("/cold-chain-boxes/{id}/segments")
    public ResponseEntity<List<TransitSegment>> getBoxSegments(@PathVariable Long id) {
        return ResponseEntity.ok(coldChainBoxService.getSegmentsByBoxId(id));
    }

    @GetMapping("/cold-chain-boxes/{id}/acceptance")
    public ResponseEntity<BoxAcceptanceRecord> getBoxAcceptance(@PathVariable Long id) {
        return ResponseEntity.ok(coldChainBoxService.getAcceptanceByBoxId(id));
    }

    @PostMapping("/cold-chain-boxes/split")
    public ResponseEntity<List<ColdChainBox>> splitBatch(@RequestBody SplitBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coldChainBoxService.splitBatch(request));
    }

    @PostMapping("/cold-chain-boxes/transfer")
    public ResponseEntity<TransitSegment> transferBox(@RequestBody TransferBoxRequest request) {
        return ResponseEntity.ok(coldChainBoxService.transferBox(request));
    }

    @PostMapping("/cold-chain-boxes/{id}/accept")
    public ResponseEntity<BoxAcceptanceRecord> acceptBox(
            @PathVariable Long id,
            @RequestBody BoxAcceptanceRequest request) {
        return ResponseEntity.ok(coldChainBoxService.acceptBox(id, request));
    }

    @PostMapping("/cold-chain-boxes/{id}/reject")
    public ResponseEntity<BoxAcceptanceRecord> rejectBox(
            @PathVariable Long id,
            @RequestBody BoxAcceptanceRequest request) {
        return ResponseEntity.ok(coldChainBoxService.rejectBox(id, request));
    }

    @PutMapping("/cold-chain-boxes/{id}/status")
    public ResponseEntity<ColdChainBox> updateBoxStatus(
            @PathVariable Long id,
            @RequestBody UpdateBoxStatusRequest request) {
        return ResponseEntity.ok(coldChainBoxService.updateBoxStatus(id, request));
    }

    @PostMapping("/cold-chain-boxes/mark-vehicle-delayed")
    public ResponseEntity<Void> markVehicleDelayed(@RequestBody Map<String, String> request) {
        String vehicleNo = request.get("vehicleNo");
        String delayReason = request.get("delayReason");
        coldChainBoxService.markVehicleDelayed(vehicleNo, delayReason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cold-chain-boxes/delayed")
    public ResponseEntity<List<ColdChainBox>> getDelayedBoxes() {
        return ResponseEntity.ok(coldChainBoxService.getDelayedBoxes());
    }
}
