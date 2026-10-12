package com.example.vendorcrm.entity;

public enum OrderStatus {
    NEW("New"),
    PROCESSING("Processing"),
    READY_TO_DISPATCH("Ready to Dispatch"),
    PENDING("Pending"),
    IN_TRANSIT("In-Transit"),
    DELIVERED("Delivered"),
    RETURNED("Returned"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Not yet handed over to the courier.
     */
    public boolean isPendingDispatch() {
        return this == NEW
                || this == PROCESSING
                || this == READY_TO_DISPATCH
                || this == PENDING;
    }
}