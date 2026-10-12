package com.example.vendorcrm.service;

import com.example.vendorcrm.entity.Payment;
import com.example.vendorcrm.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Create payment
    public Payment savePayment(Payment payment) {

        if (payment.getAmount() == null ||
                payment.getAmount().signum() < 0) {

            throw new RuntimeException(
                    "Payment amount cannot be negative");
        }

        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        return paymentRepository.save(payment);
    }

    // Get all payments
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Get payment by ID
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    // Get payment by order
    public Optional<Payment> getPaymentByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    // Get payment by transaction ID
    public Optional<Payment> getPaymentByTransactionId(
            String transactionId) {

        return paymentRepository
                .findByTransactionId(transactionId);
    }

    // Get payments by status
    public List<Payment> getPaymentsByStatus(
            String paymentStatus) {

        return paymentRepository
                .findByPaymentStatus(paymentStatus);
    }

    // Get payments between dates
    public List<Payment> getPaymentsBetweenDates(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return paymentRepository.findByPaymentDateBetween(
                startDate,
                endDate);
    }

    // Get payments by status and date
    public List<Payment> getPaymentsByStatusAndDate(
            String paymentStatus,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return paymentRepository
                .findByPaymentStatusAndPaymentDateBetween(
                        paymentStatus,
                        startDate,
                        endDate);
    }

    // Update payment
    public Payment updatePayment(
            Long id,
            Payment updatedPayment) {

        Payment existingPayment =
                paymentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found with ID: " + id));

        existingPayment.setPaymentDate(
                updatedPayment.getPaymentDate());

        existingPayment.setAmount(
                updatedPayment.getAmount());

        existingPayment.setPaymentMethod(
                updatedPayment.getPaymentMethod());

        existingPayment.setPaymentStatus(
                updatedPayment.getPaymentStatus());

        existingPayment.setTransactionId(
                updatedPayment.getTransactionId());

        existingPayment.setOrder(
                updatedPayment.getOrder());

        return paymentRepository.save(existingPayment);
    }

    // Delete payment
    public void deletePayment(Long id) {

        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException(
                    "Payment not found with ID: " + id);
        }

        paymentRepository.deleteById(id);
    }
}