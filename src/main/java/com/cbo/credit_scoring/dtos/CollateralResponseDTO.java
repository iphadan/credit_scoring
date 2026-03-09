package com.cbo.credit_scoring.dtos;

import com.cbo.credit_scoring.models.enums.CollateralType;
import com.cbo.credit_scoring.models.enums.ValuationMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollateralResponseDTO {

    private String caseId;
    private LocalDate reportingDate;
    private List<CollateralRecord> collaterals;

    // Totals
    private BigDecimal totalValue;
    private BigDecimal totalNetValue;
    private Integer totalRecords;

    // Maximum percentage collateral
    private MaxPercentageCollateral maxPercentageCollateral;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollateralRecord {
        private Long id;
        private Integer srNo;
        private CollateralType collateralType;
        private Integer yearOfManufacturing;
        private Integer age;  // Calculated
        private BigDecimal value;
        private ValuationMethod valuationMethod;
        private Boolean mandatoryMethod;  // Calculated (0 or 1)
        private BigDecimal discountRate;  // Calculated (as percentage)
        private BigDecimal netValue;      // Calculated
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaxPercentageCollateral {
        private String collateralType;
        private BigDecimal netValue;
        private BigDecimal percentage;
    }
}