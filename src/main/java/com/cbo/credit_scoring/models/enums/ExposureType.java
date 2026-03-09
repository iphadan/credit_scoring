package com.cbo.credit_scoring.models.enums;

public enum ExposureType {
    REVOLVING("Revolving Facility"),
    NON_REVOLVING("Non-Revolving Loan");

    private final String displayName;

    ExposureType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}