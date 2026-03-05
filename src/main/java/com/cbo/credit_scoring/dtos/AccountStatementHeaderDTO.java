package com.cbo.credit_scoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatementHeaderDTO {

    private Long id;

    private String caseId;

    private String accountHolder;

    private String accountNumber;

    private String industry;

    private BigDecimal sanctionLimit;

    private LocalDate approvedDate;

    private String facilityType;

    private LocalDate dateAccountOpened;

    private LocalDate reportDate;

    private String status;

    private List<AccountStatementDTO> statementRecords;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountStatementDTO {
        private Long id;
        private String caseId;

        private YearMonth month;

        private BigDecimal totalTurnoverCredit;

        private BigDecimal totalTurnoverDebit;

        private Integer numberOfCreditEntries;

        private BigDecimal monthlyCreditAverage; // Calculated field

        private BigDecimal utilizationPercentage; // Calculated field
    }
}