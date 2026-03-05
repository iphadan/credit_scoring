package com.cbo.credit_scoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OdSwingSummaryDTO {
    private Long headerId;
    private String caseId;
    private String accountHolder;
    private String accountNumber;
    private BigDecimal sanctionLimit;
    private BigDecimal averageHighestUtilization;
    private BigDecimal averageLowestUtilization;
    private BigDecimal averageUtilization;
    private BigDecimal maxUtilization;
    private BigDecimal minUtilization;
    private BigDecimal averageSwingRange;
    private BigDecimal maxSwingRange;
    private YearMonth monthWithHighestUtilization;
    private YearMonth monthWithLowestUtilization;
    private YearMonth monthWithWidestSwing;
    private Integer totalMonths;
    private Integer monthsExceedingThreshold;
    private LocalDate lastReportDate;
    private String status;
    private Map<YearMonth, BigDecimal> monthlyUtilizationTrend;
    private Map<YearMonth, BigDecimal> monthlySwingRangeTrend;
}