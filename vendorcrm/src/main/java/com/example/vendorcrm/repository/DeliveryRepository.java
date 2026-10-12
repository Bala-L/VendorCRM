package com.example.vendorcrm.repository;



import com.example.vendorcrm.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);

    Optional<Delivery> findByTrackingNumber(String trackingNumber);

    List<Delivery> findByDeliveryStatus(String deliveryStatus);

    List<Delivery> findByDeliveryDateBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Delivery> findByDeliveryStatusAndDeliveryDateBetween(
            String deliveryStatus,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}