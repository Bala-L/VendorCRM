package com.example.vendorcrm.entity;

public enum ReturnStatus {
    DISPUTE_REVIEW("Dispute Review", true),
    REFUND_PENDING_APPROVAL("Refund Pending Approval", true),
    IN_TRANSIT_TO_WAREHOUSE("In-Transit to Warehouse", false),
    REPLACEMENT_DISPATCHED("Replacement Dispatched", false),
    COMPLETED("Completed", false);

    private final String label;
    private final boolean actionRequired;

    ReturnStatus(String label, boolean actionRequired) {
        this.label = label;
        this.actionRequired = actionRequired;
    }

    public String getLabel() { return label; }
    public boolean isActionRequired() { return actionRequired; }
}
