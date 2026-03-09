package com.cbo.credit_scoring.models.enums;

public enum FacilityStatus {
    PASS("Pass"),
    SPECIAL_MENTION("Special Mention"),
    SUBSTANDARD("Substandard"),
    DOUBTFUL("Doubtful"),
    LOSS("Loss");

    private final String displayName;

    FacilityStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Helper method to get enum from display name
    public static FacilityStatus fromDisplayName(String displayName) {
        for (FacilityStatus status : FacilityStatus.values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        return PASS; // Default or throw exception
    }
}