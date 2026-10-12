package com.example.vendorcrm.controller;


import com.example.vendorcrm.entity.Delivery;
import com.example.vendorcrm.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // Create delivery
    @PostMapping
    public ResponseEntity<Delivery> createDelivery(
            @RequestBody Delivery delivery) {

        return ResponseEntity.ok(
                deliveryService.saveDelivery(delivery)
        );
    }

    // Get all deliveries
    @GetMapping
    public ResponseEntity<List<Delivery>> getAllDeliveries() {

        return ResponseEntity.ok(
                deliveryService.getAllDeliveries()
        );
    }

    // Get delivery by ID
    @GetMapping("/{id}")
    public ResponseEntity<Delivery> getDeliveryById(
            @PathVariable Long id) {

        return deliveryService.getDeliveryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get delivery by order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Delivery> getDeliveryByOrder(
            @PathVariable Long orderId) {

        return deliveryService.getDeliveryByOrder(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get delivery by tracking number
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<Delivery> getDeliveryByTrackingNumber(
            @PathVariable String trackingNumber) {

        return deliveryService
                .getDeliveryByTrackingNumber(trackingNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get deliveries by status
    @GetMapping("/status/{deliveryStatus}")
    public ResponseEntity<List<Delivery>> getDeliveriesByStatus(
            @PathVariable String deliveryStatus) {

        return ResponseEntity.ok(
                deliveryService.getDeliveriesByStatus(deliveryStatus)
        );
    }

    // Get deliveries between dates
    @GetMapping("/date-range")
    public ResponseEntity<List<Delivery>> getDeliveriesBetweenDates(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        return ResponseEntity.ok(
                deliveryService.getDeliveriesBetweenDates(
                        startDate,
                        endDate
                )
        );
    }

    // Get deliveries by status and date
    @GetMapping("/status/{deliveryStatus}/date-range")
    public ResponseEntity<List<Delivery>> getDeliveriesByStatusAndDate(
            @PathVariable String deliveryStatus,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        return ResponseEntity.ok(
                deliveryService.getDeliveriesByStatusAndDate(
                        deliveryStatus,
                        startDate,
                        endDate
                )
        );
    }

    // Update delivery
    @PutMapping("/{id}")
    public ResponseEntity<Delivery> updateDelivery(
            @PathVariable Long id,
            @RequestBody Delivery delivery) {

        return ResponseEntity.ok(
                deliveryService.updateDelivery(id, delivery)
        );
    }

    // Delete delivery
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDelivery(
            @PathVariable Long id) {

        if (deliveryService.getDeliveryById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        deliveryService.deleteDelivery(id);

        return ResponseEntity.ok(
                "Delivery deleted successfully"
        );
    }
}