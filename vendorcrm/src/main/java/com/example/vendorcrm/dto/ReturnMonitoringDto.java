package com.example.vendorcrm.dto;

public class ReturnMonitoringDto {

    private String id;
    private String orderId;
    private String item;
    private String vendor;
    private String reason;
    private String status;
    private boolean actionRequired;

    public ReturnMonitoringDto() {
    }

    public ReturnMonitoringDto(
            String id,
            String orderId,
            String item,
            String vendor,
            String reason,
            String status,
            boolean actionRequired
    ) {
        this.id = id;
        this.orderId = orderId;
        this.item = item;
        this.vendor = vendor;
        this.reason = reason;
        this.status = status;
        this.actionRequired = actionRequired;
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getItem() {
        return item;
    }

    public String getVendor() {
        return vendor;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public boolean isActionRequired() {
        return actionRequired;
    }
}