package com.cbo.credit_scoring.dtos;

import com.cbo.credit_scoring.models.enums.CollateralType;
import com.cbo.credit_scoring.models.enums.ValuationMethod;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollateralRequestDTO {

    @NotBlank(message = "Case ID is required")
    private String caseId;

    private LocalDate reportingDate;

    @NotEmpty(message = "At least one collateral record is required")
    private List<CollateralRecord> collaterals;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollateralRecord {

        private Integer srNo;

        @NotNull(message = "Collateral type is required")
        private CollateralType collateralType;

        @Min(value = 1900, message = "Year of manufacturing must be valid")
        @Max(value = 2100, message = "Year of manufacturing must be valid")
        private Integer yearOfManufacturing;

        @NotNull(message = "Value is required")
        @Positive(message = "Value must be positive")
        private BigDecimal value;

        @NotNull(message = "Valuation method is required")
        private ValuationMethod valuationMethod;
    }
}