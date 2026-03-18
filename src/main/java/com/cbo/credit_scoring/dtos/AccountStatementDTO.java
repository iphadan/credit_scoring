package com.cbo.credit_scoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatementDTO {

    private Long id;
    private String caseId; // will be added upon creation and will be used to uniquely identify the record and related table rows

    private YearMonth month;

    private BigDecimal totalTurnoverCredit;

    private BigDecimal totalTurnoverDebit;

    private Integer numberOfCreditEntries;

    private BigDecimal monthlyCreditAverage; // Calculated field

    private BigDecimal utilizationPercentage; // Calculated field

    private Long headerId;
}