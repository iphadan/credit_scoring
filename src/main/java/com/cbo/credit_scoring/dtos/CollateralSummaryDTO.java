package com.cbo.credit_scoring.dtos;

import com.cbo.credit_scoring.models.enums.CollateralType;
import com.cbo.credit_scoring.models.enums.ValuationMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollateralSummaryDTO {

    private String caseId;
    private LocalDate reportingDate;

    // Totals
    private BigDecimal totalValue;
    private BigDecimal totalNetValue;
    private Integer totalRecords;

    // Summary by collateral type
    private Map<CollateralType, TypeSummary> byCollateralType;

    // Summary by valuation method
    private Map<ValuationMethod, MethodSummary> byValuationMethod;

    // Maximum percentage collateral
    private MaxPercentageDetail maxPercentageCollateral;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TypeSummary {
        private Long count;
        private BigDecimal totalValue;
        private BigDecimal totalNetValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MethodSummary {
        private Long count;
        private BigDecimal totalValue;
        private BigDecimal totalNetValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaxPercentageDetail {
        private String collateralType;
        private BigDecimal netValue;
        private BigDecimal percentage;
    }
}