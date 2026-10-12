package com.example.vendorcrm.dto;

import java.math.BigDecimal;

public class SettlementHealthDto {

    private String fulfillmentRate;
    private String avgDispatchTime;
    private BigDecimal availablePayout;
    private BigDecimal commissionDeducted;
    private String commissionRate;

    public SettlementHealthDto() {}

    public SettlementHealthDto(
            String fulfillmentRate,
            String avgDispatchTime,
            BigDecimal availablePayout,
            BigDecimal commissionDeducted,
            String commissionRate
    ) {
        this.fulfillmentRate = fulfillmentRate;
        this.avgDispatchTime = avgDispatchTime;
        this.availablePayout = availablePayout;
        this.commissionDeducted = commissionDeducted;
        this.commissionRate = commissionRate;
    }

    public String getFulfillmentRate() {
        return fulfillmentRate;
    }

    public String getAvgDispatchTime() {
        return avgDispatchTime;
    }

    public BigDecimal getAvailablePayout() {
        return availablePayout;
    }

    public BigDecimal getCommissionDeducted() {
        return commissionDeducted;
    }

    public String getCommissionRate() {
        return commissionRate;
    }
}