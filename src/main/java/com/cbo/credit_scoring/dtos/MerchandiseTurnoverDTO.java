package com.cbo.credit_scoring.dtos;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Setter
@Getter
public class MerchandiseTurnoverDTO {
    private Long id;
    private String caseId; // will be added upon creation and will be used to uniquely identify the record and related table rows

    private YearMonth month;
    private BigDecimal debitDisbursements;
    private BigDecimal creditPrincipalRepayments;
    private Long headerId;  // Reference to the parent header
}