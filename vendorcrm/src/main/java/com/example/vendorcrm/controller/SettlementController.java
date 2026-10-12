package com.example.vendorcrm.controller;


import com.example.vendorcrm.entity.Settlement;
import com.example.vendorcrm.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    // Create settlement
    @PostMapping
    public ResponseEntity<Settlement> createSettlement(
            @RequestBody Settlement settlement) {

        return ResponseEntity.ok(
                settlementService.saveSettlement(settlement)
        );
    }

    // Get all settlements
    @GetMapping
    public ResponseEntity<List<Settlement>> getAllSettlements() {

        return ResponseEntity.ok(
                settlementService.getAllSettlements()
        );
    }

    // Get settlement by ID
    @GetMapping("/{id}")
    public ResponseEntity<Settlement> getSettlementById(
            @PathVariable Long id) {

        return settlementService.getSettlementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get settlements by vendor
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Settlement>> getSettlementsByVendor(
            @PathVariable Long vendorId) {

        return ResponseEntity.ok(
                settlementService.getSettlementsByVendor(vendorId)
        );
    }

    // Get settlements by status
    @GetMapping("/status/{settlementStatus}")
    public ResponseEntity<List<Settlement>> getSettlementsByStatus(
            @PathVariable String settlementStatus) {

        return ResponseEntity.ok(
                settlementService.getSettlementsByStatus(settlementStatus)
        );
    }

    // Get settlements between dates
    @GetMapping("/date-range")
    public ResponseEntity<List<Settlement>> getSettlementsBetweenDates(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        return ResponseEntity.ok(
                settlementService.getSettlementsBetweenDates(
                        startDate,
                        endDate
                )
        );
    }

    // Get vendor settlements between dates
    @GetMapping("/vendor/{vendorId}/date-range")
    public ResponseEntity<List<Settlement>>
    getVendorSettlementsBetweenDates(
            @PathVariable Long vendorId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        return ResponseEntity.ok(
                settlementService.getVendorSettlementsBetweenDates(
                        vendorId,
                        startDate,
                        endDate
                )
        );
    }

    // Get vendor settlements by status
    @GetMapping("/vendor/{vendorId}/status/{settlementStatus}")
    public ResponseEntity<List<Settlement>>
    getVendorSettlementsByStatus(
            @PathVariable Long vendorId,
            @PathVariable String settlementStatus) {

        return ResponseEntity.ok(
                settlementService.getVendorSettlementsByStatus(
                        vendorId,
                        settlementStatus
                )
        );
    }

    // Update settlement
    @PutMapping("/{id}")
    public ResponseEntity<Settlement> updateSettlement(
            @PathVariable Long id,
            @RequestBody Settlement settlement) {

        return ResponseEntity.ok(
                settlementService.updateSettlement(
                        id,
                        settlement
                )
        );
    }

    // Delete settlement
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSettlement(
            @PathVariable Long id) {

        if (settlementService.getSettlementById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        settlementService.deleteSettlement(id);

        return ResponseEntity.ok(
                "Settlement deleted successfully"
        );
    }
}