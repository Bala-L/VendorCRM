package com.example.vendorcrm.dto;

public class InventoryHealthDto {

    private String sku;
    private String vendor;
    private String name;
    private int currentStock;
    private int reorderLevel;
    private String status;

    public InventoryHealthDto() {
    }

    public InventoryHealthDto(
            String sku,
            String vendor,
            String name,
            int currentStock,
            int reorderLevel,
            String status
    ) {
        this.sku = sku;
        this.vendor = vendor;
        this.name = name;
        this.currentStock = currentStock;
        this.reorderLevel = reorderLevel;
        this.status = status;
    }

    public String getSku() {
        return sku;
    }

    public String getVendor() {
        return vendor;
    }

    public String getName() {
        return name;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public String getStatus() {
        return status;
    }
}