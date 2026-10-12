package com.example.vendorcrm.dto;

import java.math.BigDecimal;

/** onTimeRate / avgTransitDays are null when no deliveries in the period */
public class CarrierMonitoringDto {

    private String carrier;
    private long activeShipments;
    private BigDecimal onTimeRate;
    private BigDecimal avgTransitDays;
    private String status;

    public CarrierMonitoringDto() {
    }

    public CarrierMonitoringDto(
            String carrier,
            long activeShipments,
            BigDecimal onTimeRate,
            BigDecimal avgTransitDays,
            String status
    ) {
        this.carrier = carrier;
        this.activeShipments = activeShipments;
        this.onTimeRate = onTimeRate;
        this.avgTransitDays = avgTransitDays;
        this.status = status;
    }

    public String getCarrier() {
        return carrier;
    }

    public long getActiveShipments() {
        return activeShipments;
    }

    public BigDecimal getOnTimeRate() {
        return onTimeRate;
    }

    public BigDecimal getAvgTransitDays() {
        return avgTransitDays;
    }

    public String getStatus() {
        return status;
    }
}