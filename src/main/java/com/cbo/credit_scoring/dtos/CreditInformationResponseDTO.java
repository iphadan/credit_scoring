package com.cbo.credit_scoring.dtos;

import com.cbo.credit_scoring.models.enums.*;
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
public class CreditInformationResponseDTO {

    private Long id;
    private String caseId;
    private BankType bankType;
    private LocalDate reportingDate;
    private List<ExposureRecord> exposures;

    // Calculated totals (not stored in DB)
    private BigDecimal totalAmountGranted;
    private BigDecimal totalCurrentBalance;
    private Integer totalRecords;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExposureRecord {
        private Long id;
        private Integer srNo;
        private ExposureType exposureType;
        private String existingExposure;
        private String lendingBank;
        private CreditProductType creditProduct;
        private LocalDate dateGranted;
        private LocalDate expiryDate;
        private BankType bankType;

        private BigDecimal amountGranted;
        private BigDecimal currentBalance;
        private FacilityStatus status;
    }
}