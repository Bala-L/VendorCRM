package com.example.vendorcrm.controller;


import com.example.vendorcrm.entity.Payment;
import com.example.vendorcrm.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Create payment
    @PostMapping
    public ResponseEntity<Payment> createPayment(
            @RequestBody Payment payment) {

        return ResponseEntity.ok(
                paymentService.savePayment(payment)
        );
    }

    // Get all payments
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {

        return ResponseEntity.ok(
                paymentService.getAllPayments()
        );
    }

    // Get payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(
            @PathVariable Long id) {

        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get payment by order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrder(
            @PathVariable Long orderId) {

        return paymentService.getPaymentByOrder(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get payment by transaction ID
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(
            @PathVariable String transactionId) {

        return paymentService
                .getPaymentByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get payments by status
    @GetMapping("/status/{paymentStatus}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(
            @PathVariable String paymentStatus) {

        return ResponseEntity.ok(
                paymentService.getPaymentsByStatus(paymentStatus)
        );
    }

    // Get payments between dates
    @GetMapping("/date-range")
    public ResponseEntity<List<Payment>> getPaymentsBetweenDates(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        return ResponseEntity.ok(
                paymentService.getPaymentsBetweenDates(
                        startDate,
                        endDate
                )
        );
    }

    // Get payments by status and date
    @GetMapping("/status/{paymentStatus}/date-range")
    public ResponseEntity<List<Payment>> getPaymentsByStatusAndDate(
            @PathVariable String paymentStatus,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        return ResponseEntity.ok(
                paymentService.getPaymentsByStatusAndDate(
                        paymentStatus,
                        startDate,
                        endDate
                )
        );
    }

    // Update payment
    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(
            @PathVariable Long id,
            @RequestBody Payment payment) {

        return ResponseEntity.ok(
                paymentService.updatePayment(id, payment)
        );
    }

    // Delete payment
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePayment(
            @PathVariable Long id) {

        if (paymentService.getPaymentById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        paymentService.deletePayment(id);

        return ResponseEntity.ok(
                "Payment deleted successfully"
        );
    }
}