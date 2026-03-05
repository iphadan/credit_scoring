package com.cbo.credit_scoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OdTurnoverSummaryDTO {
    private Long headerId;
    private String caseId;
    private String accountHolder;
    private String accountNumber;
    private BigDecimal sanctionLimit;
    private BigDecimal totalCredit;
    private BigDecimal totalDebit;
    private BigDecimal netTurnover;
    private BigDecimal averageMonthlyCredit;
    private BigDecimal averageMonthlyDebit;
    private BigDecimal averageUtilization;
    private Integer totalMonths;
    private Integer activeMonths;
    private YearMonth lastTransactionMonth;
    private Map<YearMonth, BigDecimal> monthlyUtilizationTrend;
    private String status;
}