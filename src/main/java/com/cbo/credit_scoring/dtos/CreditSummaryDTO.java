package com.cbo.credit_scoring.dtos;

import com.cbo.credit_scoring.models.enums.BankType;
import com.cbo.credit_scoring.models.enums.ExposureType;
import com.cbo.credit_scoring.models.enums.FacilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditSummaryDTO {

    private String caseId;
    private LocalDate reportingDate;

    // Coop Bank Summary
    private BankSummary coopBankSummary;

    // Other Bank Summary
    private BankSummary otherBankSummary;

    // Grand Totals
    private BigDecimal grandTotalAmountGranted;
    private BigDecimal grandTotalCurrentBalance;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BankSummary {
        private BankType bankType;
        private BigDecimal totalAmountGranted;
        private BigDecimal totalCurrentBalance;
        private Integer totalRecords;

        // Breakdown by exposure type
        private Map<ExposureType, TypeSummary> byExposureType;

        // Breakdown by status
        private Map<FacilityStatus, StatusSummary> byStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TypeSummary {
        private Long count;
        private BigDecimal totalAmountGranted;
        private BigDecimal totalCurrentBalance;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusSummary {
        private Long count;
        private BigDecimal totalAmountGranted;
        private BigDecimal totalCurrentBalance;
    }
}