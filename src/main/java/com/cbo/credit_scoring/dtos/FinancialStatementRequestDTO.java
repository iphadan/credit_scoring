package com.cbo.credit_scoring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialStatementRequestDTO {

    @NotBlank(message = "Case ID is required")
    private String caseId; // Reference to the parent case

    private String companyName;
    private String statementType;
    private LocalDate reportingDate;

    @NotEmpty(message = "At least one period is required")
    @Size(min = 4, max = 4, message = "Exactly 4 periods are required for financial statements")
    private List<PeriodInput> periods;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeriodInput {

        @NotNull(message = "Period date is required")
        private LocalDate periodDate;

        private Boolean isAudited = true;

        // Balance Sheet fields
        private BigDecimal propertyPlantEquipment;
        private BigDecimal stocks;
        private BigDecimal tradeOtherReceivables;
        private BigDecimal cashOnHandBank;
        private BigDecimal tradeOtherPayables;
        private BigDecimal otherPayables;
        private BigDecimal shareholderAccount;
        private BigDecimal profitTaxPayable;
        private BigDecimal bankLoan;
        private BigDecimal leasePayable;
        private BigDecimal capital;
        private BigDecimal reserve;
        private BigDecimal retainedEarnings;

        // Income Statement fields
        private BigDecimal sales;
        private BigDecimal costOfGoodsSold;
        private BigDecimal operatingExpenses;
        private BigDecimal depreciationExpense;
        private BigDecimal interestExpense;
//        private BigDecimal taxRate;
    }
}