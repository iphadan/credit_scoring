package com.cbo.credit_scoring.dtos;

import com.cbo.credit_scoring.models.enums.*;
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
public class CreditInformationRequestDTO {

    @NotBlank(message = "Case ID is required")
    private String caseId;

    @NotNull(message = "Bank type is required")
    private BankType bankType;

    private LocalDate reportingDate;

    @NotEmpty(message = "At least one exposure record is required")
    @Size(min = 1, message = "At least one exposure record is required")
    private List<ExposureRecord> exposures;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExposureRecord {

        private Integer srNo;

        @NotNull(message = "Exposure type is required")
        private ExposureType exposureType;

        private String existingExposure;

        private String lendingBank;


        @NotNull(message = "Credit product is required")
        private CreditProductType creditProduct;

        @PastOrPresent(message = "Date granted cannot be in the future")
        private LocalDate dateGranted;

        @Future(message = "Expiry date must be in the future")
        private LocalDate expiryDate;

        @NotNull(message = "Amount granted is required")
        @Positive(message = "Amount granted must be positive")
        @DecimalMin(value = "0.01", message = "Amount granted must be at least 0.01")
        private BigDecimal amountGranted;

        @NotNull(message = "Current balance is required")
        @PositiveOrZero(message = "Current balance cannot be negative")
        private BigDecimal currentBalance;

        @NotNull(message = "Status is required")
        private FacilityStatus status;


    }
}