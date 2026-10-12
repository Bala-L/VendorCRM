package com.example.vendorcrm.dto;

import java.math.BigDecimal;

public class OperationalOrderDto {

    private String id;
    private String vendor;
    private String customer;
    private String items;
    private BigDecimal amount;
    private String courier;
    private String sla;
    private boolean slaBreached;
    private String status;

    public OperationalOrderDto() {
    }

    public OperationalOrderDto(
            String id,
            String vendor,
            String customer,
            String items,
            BigDecimal amount,
            String courier,
            String sla,
            boolean slaBreached,
            String status
    ) {
        this.id = id;
        this.vendor = vendor;
        this.customer = customer;
        this.items = items;
        this.amount = amount;
        this.courier = courier;
        this.sla = sla;
        this.slaBreached = slaBreached;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getVendor() {
        return vendor;
    }

    public String getCustomer() {
        return customer;
    }

    public String getItems() {
        return items;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCourier() {
        return courier;
    }

    public String getSla() {
        return sla;
    }

    public boolean isSlaBreached() {
        return slaBreached;
    }

    public String getStatus() {
        return status;
    }
}