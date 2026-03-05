package com.cbo.credit_scoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OdTurnoverDTO {

    private Long id;

    private YearMonth month;

    private BigDecimal totalTurnoverCredit;

    private BigDecimal totalTurnoverDebit;

    private Integer numberOfCreditEntries;

    private BigDecimal monthlyCreditAverage; // Calculated field

    private BigDecimal utilizationPercentage; // Calculated field

    private Long headerId;
}