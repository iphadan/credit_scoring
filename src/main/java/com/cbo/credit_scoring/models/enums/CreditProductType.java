package com.cbo.credit_scoring.models.enums;

public enum CreditProductType {
    REVOLVING_PRE_SHIPMENT("Revolving Pre-Shipment"),
    REVOLVING_MERCHANDISE("Revolving Merchandise"),
    EXPORT_OVERDRAFT("Export Overdraft"),
    IMPORT_OVERDRAFT("Import Overdraft"),
    MANUFACTURING_OVERDRAFT("Manufacturing Overdraft"),
    DTS_OVERDRAFT("DTS Overdraft"),
    BUILDING_CONSTRUCTION_OVERDRAFT("Building & Construction Overdraft"),
    OTHER("Other");

    private final String displayName;

    CreditProductType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Helper method to get enum from display name
    public static CreditProductType fromDisplayName(String displayName) {
        for (CreditProductType type : CreditProductType.values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        return OTHER;
    }
}