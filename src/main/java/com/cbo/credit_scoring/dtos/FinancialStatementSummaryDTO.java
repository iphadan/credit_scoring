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
public class FinancialStatementSummaryDTO {

    private Long id;
    private String caseId;
    private String companyName;
    private LocalDate reportingDate;
    private Integer version;

    // Key Financial Highlights
    private BigDecimal totalRevenue;
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal totalEquity;
    private BigDecimal netIncome;

    // Growth Rates
    private BigDecimal revenueGrowth;
    private BigDecimal assetGrowth;
    private BigDecimal incomeGrowth;

    // Key Ratios
    private Map<String, BigDecimal> keyRatios;

    // Period Performance
    private Map<LocalDate, BigDecimal> revenueTrend;
    private Map<LocalDate, BigDecimal> netIncomeTrend;
    private Map<LocalDate, BigDecimal> assetTrend;

    // Summary Statistics
    private Integer totalPeriods;
    private LocalDate oldestPeriod;
    private LocalDate newestPeriod;
    private String financialHealth; // HEALTHY, MODERATE, WEAK
    private List<String> warnings;
    private List<String> strengths;
}