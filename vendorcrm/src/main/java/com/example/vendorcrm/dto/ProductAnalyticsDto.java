package com.example.vendorcrm.dto;

import java.math.BigDecimal;

public record ProductAnalyticsDto(
        String id,        // SKU
        String name,
        String category,
        String vendor,    // vendor name
        long sold,
        BigDecimal revenue,
        int stock,
        String status     // In Stock / Low Stock / Out of Stock
) {}