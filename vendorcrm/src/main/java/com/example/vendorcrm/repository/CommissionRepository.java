package com.example.vendorcrm.repository;


import com.example.vendorcrm.entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CommissionRepository extends JpaRepository<Commission, Long> {

    List<Commission> findByVendorId(Long vendorId);

    List<Commission> findByOrderId(Long orderId);

    List<Commission> findByCommissionStatus(String commissionStatus);

    List<Commission> findByCommissionDateBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Commission> findByVendorIdAndCommissionDateBetween(
            Long vendorId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Commission> findByVendorIdAndCommissionStatus(
            Long vendorId,
            String commissionStatus
    );
}