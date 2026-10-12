package com.example.vendorcrm.dto;

import com.example.vendorcrm.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Lightweight projection used for aggregation (no lazy loading). */
public record OrderRow(
        OrderStatus status,
        LocalDateTime orderDate,
        BigDecimal totalAmount,
        BigDecimal totalCost,
        BigDecimal commissionAmount,
        Long customerId
) {}