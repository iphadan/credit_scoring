package com.cbo.credit_scoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseSummaryDTO {

    // Counts by module
    private Integer preShipmentCount;
    private Integer odTurnoverCount;
    private Integer odSwingCount;
    private Integer merchandiseCount;
    private Integer accountStatementCount;
    private Integer financialStatementCount;
    private Integer creditInformationCount;
    private Integer collateralCount;

    // Financial totals
    private BigDecimal totalPreShipmentAmount;
    private BigDecimal totalOdTurnoverAmount;
    private BigDecimal totalCollateralValue;
    private BigDecimal totalCollateralNetValue;

    // Module availability flags
    private Map<String, Boolean> modulesAvailable;

    // Latest reporting dates
    private Map<String, String> latestReportingDates;
}