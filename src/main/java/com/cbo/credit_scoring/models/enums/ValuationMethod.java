package com.cbo.credit_scoring.models.enums;

public enum ValuationMethod {
    INSURANCE("Insurance"),
    ENGINEER("Engineer"),
    INVOICE("Invoice");

    private final String displayName;

    ValuationMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}