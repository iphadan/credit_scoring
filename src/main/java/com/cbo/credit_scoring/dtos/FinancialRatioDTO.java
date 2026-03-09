package com.cbo.credit_scoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRatioDTO {

    // Efficiency Ratios
    private BigDecimal activityRatio;           // Sales Growth %
    private BigDecimal inventoryTurnover;        // Inventory Turnover
    private BigDecimal collectionPeriod;         // Collection Period (days)
    private BigDecimal workingCapitalToSales;    // Working Capital to Sales

    // Financial Strength Ratios
    private BigDecimal currentRatio;             // Current Ratio
    private BigDecimal acidTestRatio;            // Acid Test Ratio
    private BigDecimal equityToTotalAsset;       // Equity to Total Asset %
    private BigDecimal debtToTotalAsset;         // Debt to Total Asset %
    private BigDecimal debtToNetWorth;           // Debt to Net Worth

    // Profitability Ratios
    private BigDecimal roa;                       // Return on Assets %
    private BigDecimal roe;                       // Return on Equity %
    private BigDecimal debtServiceCoverageRatio; // Debt Service Coverage Ratio
    private BigDecimal operatingProfitMargin;     // Operating Profit Margin %
    private BigDecimal grossProfitMargin;         // Gross Profit Margin %
    private BigDecimal netProfitMargin;           // Net Profit Margin %

    // Additional Ratios
    private BigDecimal assetTurnover;             // Asset Turnover
    private BigDecimal fixedAssetTurnover;        // Fixed Asset Turnover
    private BigDecimal interestCoverageRatio;     // Interest Coverage Ratio

    // Metadata
    private Integer periodOrder;                   // Which period this ratio belongs to
    private String periodDate;                      // Period date as string
}