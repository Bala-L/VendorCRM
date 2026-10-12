package com.example.vendorcrm.repository;



import com.example.vendorcrm.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByVendorId(Long vendorId);

    List<Settlement> findBySettlementStatus(String settlementStatus);

    List<Settlement> findBySettlementDateBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Settlement> findByVendorIdAndSettlementDateBetween(
            Long vendorId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<Settlement> findByVendorIdAndSettlementStatus(
            Long vendorId,
            String settlementStatus
    );
}