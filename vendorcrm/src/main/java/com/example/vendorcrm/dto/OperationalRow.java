package com.example.vendorcrm.dto;

import com.example.vendorcrm.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Lightweight projection for command-centre aggregation (no lazy loading). */
public record OperationalRow(
        OrderStatus status,
        LocalDateTime orderDate,
        LocalDateTime slaDueAt,
        LocalDateTime dispatchedAt,
        LocalDateTime deliveredAt,
        String courierName,
        Integer courierSlaHours,
        BigDecimal totalAmount,
        BigDecimal commissionAmount
) {}
