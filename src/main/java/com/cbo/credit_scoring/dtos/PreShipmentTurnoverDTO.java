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
    public class PreShipmentTurnoverDTO {
        private Long id;
        private YearMonth month;
        private BigDecimal debitDisbursements;
        private BigDecimal creditPrincipalRepayments;
        private Long headerId;  // Reference to the parent header
    }
