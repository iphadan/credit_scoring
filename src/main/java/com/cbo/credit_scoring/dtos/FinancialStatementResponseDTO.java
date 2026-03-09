package com.cbo.credit_scoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialStatementResponseDTO {

    private Long id;
    private String caseId;
    private String companyName;
    private LocalDate reportingDate;
    private Integer version;

    // Period data (4 periods)
    private List<PeriodData> periods;

    // Averages across all periods
    private PeriodData averages;

    // Ratios for each period and averages
    private Map<String, RatioData> ratios;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeriodData {
        private LocalDate periodDate;
        private Boolean isAudited;
        private Integer periodOrder;

        // Balance Sheet (Raw and Calculated)
        private BigDecimal propertyPlantEquipment;
        private BigDecimal totalNonCurrentAssets;
        private BigDecimal stocks;
        private BigDecimal tradeOtherReceivables;
        private BigDecimal cashOnHandBank;
        private BigDecimal totalCurrentAssets;
        private BigDecimal totalAssets;

        private BigDecimal tradeOtherPayables;
        private BigDecimal otherPayables;
        private BigDecimal shareholderAccount;
        private BigDecimal profitTaxPayable;
        private BigDecimal totalCurrentLiabilities;
        private BigDecimal bankLoan;
        private BigDecimal leasePayable;
        private BigDecimal totalLongTermLiabilities;
        private BigDecimal totalLiabilities;

        private BigDecimal capital;
        private BigDecimal reserve;
        private BigDecimal retainedEarnings;
        private BigDecimal totalCapital;
        private BigDecimal totalLiabilitiesAndCapital;

        private BigDecimal netWorkingCapital;
        private BigDecimal tangibleNetWorth;

        // Income Statement (Raw and Calculated)
        private BigDecimal sales;
        private BigDecimal costOfGoodsSold;
        private BigDecimal grossProfit;
        private BigDecimal operatingExpenses;
        private BigDecimal profitBeforeInterestTaxDepreciation;
        private BigDecimal depreciationExpense;
        private BigDecimal profitBeforeInterestAndTax;
        private BigDecimal interestExpense;
        private BigDecimal profitBeforeTax;
        private BigDecimal incomeTax;
        private BigDecimal netIncome;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RatioData {
        private String name;
        private String description;
        private List<BigDecimal> periodValues;
        private BigDecimal average;

        // Efficiency Ratios
        private BigDecimal activityRatio;
        private BigDecimal inventoryTurnover;
        private BigDecimal collectionPeriod;
        private BigDecimal workingCapitalToSales;

        // Financial Strength Ratios
        private BigDecimal currentRatio;
        private BigDecimal acidTestRatio;
        private BigDecimal equityToTotalAsset;
        private BigDecimal debtToTotalAsset;
        private BigDecimal debtToNetWorth;

        // Profitability Ratios
        private BigDecimal roa;
        private BigDecimal roe;
        private BigDecimal debtServiceCoverageRatio;
        private BigDecimal operatingProfitMargin;
        private BigDecimal grossProfitMargin;
        private BigDecimal netProfitMargin;
    }
}