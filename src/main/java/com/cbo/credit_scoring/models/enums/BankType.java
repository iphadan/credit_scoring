package com.cbo.credit_scoring.models.enums;

public enum BankType {
    COOP_BANK("Cooperative Bank"),
    OTHER_BANK("Other Bank");

    private final String displayName;

    BankType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}