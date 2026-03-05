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
public class AccountStatementSummaryDTO {
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
    private BigDecimal maxUtilization;
    private BigDecimal minUtilization;
    private Integer totalMonths;
    private Integer activeMonths;
    private Integer totalCreditEntries;
    private YearMonth lastTransactionMonth;
    private YearMonth highestUtilizationMonth;
    private YearMonth lowestUtilizationMonth;
    private LocalDate dateAccountOpened;
    private LocalDate reportDate;
    private String status;
    private Map<YearMonth, BigDecimal> monthlyUtilizationTrend;
    private Map<YearMonth, BigDecimal> monthlyCreditTrend;
    private Map<YearMonth, BigDecimal> monthlyDebitTrend;
}