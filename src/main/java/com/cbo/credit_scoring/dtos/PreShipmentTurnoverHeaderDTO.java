package com.cbo.credit_scoring.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreShipmentTurnoverHeaderDTO {
    private Long id;
    private String customerName;
    private String caseId;
    private String typeOfFacility;
    private String industryType;
    private String accountNumber;
    private BigDecimal approvedAmount;
    private LocalDate dateApproved;
    private LocalDate reportingDate;
    private List<PreShipmentTurnoverHeaderDTO.PreShipmentTurnoverDTO> turnoverRecords;

    @Data
    @Setter
    @Getter
    public static class PreShipmentTurnoverDTO {
        private Long id;
        private String caseId;
        private YearMonth month;
        private BigDecimal debitDisbursements;
        private BigDecimal creditPrincipalRepayments;

    }
}