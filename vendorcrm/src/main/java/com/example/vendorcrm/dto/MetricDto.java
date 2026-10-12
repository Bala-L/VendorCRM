package com.example.vendorcrm.dto;

import java.math.BigDecimal;

/**
 * format: CURRENCY | NUMBER | PERCENT | HOURS | SKU_COUNT
 * compareValue (optional) is appended to subtext by the UI, e.g. "vs yesterday" + "₹29,100"
 */
public record MetricDto(
        String key,
        String title,
        BigDecimal value,
        String format,
        String subtext,
        BigDecimal compareValue
) {}
