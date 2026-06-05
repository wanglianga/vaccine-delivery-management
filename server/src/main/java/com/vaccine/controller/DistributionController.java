package com.vaccine.controller;

import com.vaccine.dto.CreateDistributionRequest;
import com.vaccine.entity.DistributionOrder;
import com.vaccine.service.DistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DistributionController {

    @Autowired
    private DistributionService distributionService;

    @GetMapping("/distribution-orders")
    public ResponseEntity<List<DistributionOrder>> getOrders(
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(distributionService.getOrdersByStatus(status));
        }
        return ResponseEntity.ok(distributionService.getAllOrders());
    }

    @PostMapping("/distribution-orders")
    public ResponseEntity<DistributionOrder> createOrder(@RequestBody CreateDistributionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(distributionService.createOrder(request));
    }
}
