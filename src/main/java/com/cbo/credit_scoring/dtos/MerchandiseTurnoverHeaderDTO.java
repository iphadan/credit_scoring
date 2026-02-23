package com.cbo.credit_scoring.dtos;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Data
public class MerchandiseTurnoverHeaderDTO {
    private Long id;
    private String customerName;
    private String caseId;
    private String typeOfFacility;
    private String industryType;
    private String accountNumber;
    private BigDecimal approvedAmount;
    private LocalDate dateApproved;
    private LocalDate reportingDate;
    private List<MerchandiseTurnoverDTO> turnoverRecords;

    @Data
    @Setter
    @Getter
    public static class MerchandiseTurnoverDTO {
        private Long id;
        private YearMonth month;
        private BigDecimal debitDisbursements;
        private BigDecimal creditPrincipalRepayments;
    }
}