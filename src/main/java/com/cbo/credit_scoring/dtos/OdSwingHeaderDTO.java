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
public class OdSwingHeaderDTO {

    private Long id;

    private String caseId;

    private String accountHolder;

    private String accountNumber;

    private String industry;

    private BigDecimal sanctionLimit;

    private LocalDate approvedDate;

    private String facilityType;

    private LocalDate reportDate;

    private String status;

    private List<OdSwingDTO> swingRecords;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OdSwingDTO {
        private Long id;
        private String caseId;

        private YearMonth month;

        private BigDecimal highestUtilization;

        private LocalDate dateHigh;

        private BigDecimal lowestUtilization;

        private LocalDate dateLow;

        private BigDecimal utilizationPercentage;
    }
}