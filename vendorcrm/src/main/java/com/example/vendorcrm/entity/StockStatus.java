package com.example.vendorcrm.entity;

public enum StockStatus {
    IN_STOCK("In Stock"),
    LOW_STOCK("Low Stock"),
    OUT_OF_STOCK("Out of Stock");

    private final String label;

    StockStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * null / "All" -> null (no filter).
     * Accepts label or enum name.
     */
    public static StockStatus fromLabel(String s) {
        if (s == null || s.isBlank() || s.equalsIgnoreCase("All")) {
            return null;
        }

        for (StockStatus status : values()) {
            if (status.label.equalsIgnoreCase(s.trim())
                    || status.name().equalsIgnoreCase(s.trim())) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid stock status: " + s);
    }
}