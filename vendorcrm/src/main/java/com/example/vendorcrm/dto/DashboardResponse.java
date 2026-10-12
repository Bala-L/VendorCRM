package com.example.vendorcrm.dto;

import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
        String range,
        LocalDate from,
        LocalDate to,
        List<KpiDto> kpis,
        List<TrendPointDto> trend,
        List<CategoryShareDto> categoryShare,
        List<OrderStatusDto> orderStatus
) {}