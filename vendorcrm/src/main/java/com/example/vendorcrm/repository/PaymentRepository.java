package com.example.vendorcrm.repository;


import com.example.vendorcrm.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByPaymentStatus(String paymentStatus);

    List<Payment> findByPaymentDateBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Payment> findByPaymentStatusAndPaymentDateBetween(
            String paymentStatus,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}