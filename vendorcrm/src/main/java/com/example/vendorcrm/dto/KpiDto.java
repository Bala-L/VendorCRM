package com.example.vendorcrm.dto;

import java.math.BigDecimal;

/**
 * format: CURRENCY | NUMBER | PERCENT
 * changePercent: vs previous period of same length (null = not available)
 * positiveIsGood: false for metrics where a decrease is good (return rate, cancellations)
 */
public record KpiDto(
        String key,
        String title,
        BigDecimal value,
        String format,
        Double changePercent,
        boolean positiveIsGood,
        String note
) {}