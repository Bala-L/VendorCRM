package com.example.vendorcrm.controller;



import com.example.vendorcrm.entity.Commission;
import com.example.vendorcrm.service.CommissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/commissions")
public class CommissionController {

    private final CommissionService commissionService;

    public CommissionController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    // Create commission
    @PostMapping
    public ResponseEntity<Commission> createCommission(
            @RequestBody Commission commission) {

        return ResponseEntity.ok(
                commissionService.saveCommission(commission)
        );
    }

    // Get all commissions
    @GetMapping
    public ResponseEntity<List<Commission>> getAllCommissions() {

        return ResponseEntity.ok(
                commissionService.getAllCommissions()
        );
    }

    // Get commission by ID
    @GetMapping("/{id}")
    public ResponseEntity<Commission> getCommissionById(
            @PathVariable Long id) {

        return commissionService.getCommissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get commissions by vendor
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<Commission>> getCommissionsByVendor(
            @PathVariable Long vendorId) {

        return ResponseEntity.ok(
                commissionService.getCommissionsByVendor(vendorId)
        );
    }

    // Get commissions by order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Commission>> getCommissionsByOrder(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                commissionService.getCommissionsByOrder(orderId)
        );
    }

    // Get commissions by status
    @GetMapping("/status/{commissionStatus}")
    public ResponseEntity<List<Commission>> getCommissionsByStatus(
            @PathVariable String commissionStatus) {

        return ResponseEntity.ok(
                commissionService.getCommissionsByStatus(
                        commissionStatus
                )
        );
    }

    // Get commissions between dates
    @GetMapping("/date-range")
    public ResponseEntity<List<Commission>> getCommissionsBetweenDates(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        return ResponseEntity.ok(
                commissionService.getCommissionsBetweenDates(
                        startDate,
                        endDate
                )
        );
    }

    // Get vendor commissions between dates
    @GetMapping("/vendor/{vendorId}/date-range")
    public ResponseEntity<List<Commission>>
    getVendorCommissionsBetweenDates(
            @PathVariable Long vendorId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        return ResponseEntity.ok(
                commissionService.getVendorCommissionsBetweenDates(
                        vendorId,
                        startDate,
                        endDate
                )
        );
    }

    // Get vendor commissions by status
    @GetMapping("/vendor/{vendorId}/status/{commissionStatus}")
    public ResponseEntity<List<Commission>>
    getVendorCommissionsByStatus(
            @PathVariable Long vendorId,
            @PathVariable String commissionStatus) {

        return ResponseEntity.ok(
                commissionService.getVendorCommissionsByStatus(
                        vendorId,
                        commissionStatus
                )
        );
    }

    // Update commission
    @PutMapping("/{id}")
    public ResponseEntity<Commission> updateCommission(
            @PathVariable Long id,
            @RequestBody Commission commission) {

        return ResponseEntity.ok(
                commissionService.updateCommission(
                        id,
                        commission
                )
        );
    }

    // Delete commission
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCommission(
            @PathVariable Long id) {

        if (commissionService.getCommissionById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        commissionService.deleteCommission(id);

        return ResponseEntity.ok(
                "Commission deleted successfully"
        );
    }
}