package com.cbo.credit_scoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OdSwingDTO {

    private Long id;

    private YearMonth month;

    private BigDecimal highestUtilization;

    private LocalDate dateHigh;

    private BigDecimal lowestUtilization;

    private LocalDate dateLow;

    private BigDecimal utilizationPercentage;

    private Long headerId;
}