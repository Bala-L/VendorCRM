package com.example.vendorcrm.controller;

import com.example.vendorcrm.entity.Return;
import com.example.vendorcrm.service.ReturnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    // Create return
    @PostMapping
    public ResponseEntity<Return> createReturn(
            @RequestBody Return returnEntity) {

        return ResponseEntity.ok(
                returnService.saveReturn(returnEntity)
        );
    }

    // Get all returns
    @GetMapping
    public ResponseEntity<List<Return>> getAllReturns() {

        return ResponseEntity.ok(
                returnService.getAllReturns()
        );
    }

    // Get return by ID
    @GetMapping("/{id}")
    public ResponseEntity<Return> getReturnById(
            @PathVariable Long id) {

        return returnService.getReturnById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get return by order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Return> getReturnByOrder(
            @PathVariable Long orderId) {

        return returnService.getReturnByOrder(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get returns by status
    @GetMapping("/status/{returnStatus}")
    public ResponseEntity<List<Return>> getReturnsByStatus(
            @PathVariable String returnStatus) {

        return ResponseEntity.ok(
                returnService.getReturnsByStatus(returnStatus)
        );
    }

    // Get returns by refund status
    @GetMapping("/refund-status/{refundStatus}")
    public ResponseEntity<List<Return>> getReturnsByRefundStatus(
            @PathVariable String refundStatus) {

        return ResponseEntity.ok(
                returnService.getReturnsByRefundStatus(refundStatus)
        );
    }

    // Get returns between dates
    @GetMapping("/date-range")
    public ResponseEntity<List<Return>> getReturnsBetweenDates(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        return ResponseEntity.ok(
                returnService.getReturnsBetweenDates(
                        startDate,
                        endDate
                )
        );
    }

    // Get returns by status and date
    @GetMapping("/status/{returnStatus}/date-range")
    public ResponseEntity<List<Return>> getReturnsByStatusAndDate(
            @PathVariable String returnStatus,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        return ResponseEntity.ok(
                returnService.getReturnsByStatusAndDate(
                        returnStatus,
                        startDate,
                        endDate
                )
        );
    }

    // Update return
    @PutMapping("/{id}")
    public ResponseEntity<Return> updateReturn(
            @PathVariable Long id,
            @RequestBody Return returnEntity) {

        return ResponseEntity.ok(
                returnService.updateReturn(id, returnEntity)
        );
    }

    // Delete return
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReturn(
            @PathVariable Long id) {

        if (returnService.getReturnById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        returnService.deleteReturn(id);

        return ResponseEntity.ok(
                "Return deleted successfully"
        );
    }
}