
package com.example.vendorcrm.repository;

import com.example.vendorcrm.entity.Return;
import com.example.vendorcrm.entity.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReturnRepository extends JpaRepository<Return, Long> {

    Optional<Return> findByCustomerOrderId(Long orderId);

    List<Return> findByStatus(ReturnStatus status);

    List<Return> findByRefundStatus(String refundStatus);

    List<Return> findByCreatedAtBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Return> findByStatusAndCreatedAtBetween(
            ReturnStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}

