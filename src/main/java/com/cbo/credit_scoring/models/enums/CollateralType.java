package com.cbo.credit_scoring.models.enums;

public enum CollateralType {
    VEHICLE("Vehicle"),
    MERCHANDIZE("Merchandize"),
    CASH("Cash"),
    CASH_SUBSTITUTES("Cash Substitutes"),
    BUILDING("Building"),
    SHARE_CERTIFICATE("Share certificate"),
    MACHINERY("Machinery"),
    CORPORATE_GUARANTEE("Corporate Guarantee"),
    GOVERNMENT_GUARANTEE("Government Guarantee"),
    BANK_GUARANTEE("Bank Guarantee"),
    PERSONAL_GUARANTEE("Personal Guarantee"),
    EXPORT_DOCUMENT("Export Document"),
    LAND_LEASE_RIGHT("Land Lease Right"),
    COFFEE_PLANTATION("Coffee Plantation"),
    SECOND_DEGREE_MORTGAGE("Second Degree Mortgage");

    private final String displayName;

    CollateralType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}