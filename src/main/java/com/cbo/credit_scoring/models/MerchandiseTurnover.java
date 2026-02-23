package com.cbo.credit_scoring.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "merchandise_turnover")
@Data
@Setter
@Getter
public class MerchandiseTurnover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "turnover_month")
    private YearMonth month;  // Using YearMonth to store month and year

    @Column(name = "debit_disbursements", precision = 15, scale = 2)
    private BigDecimal debitDisbursements;

    @Column(name = "credit_principal_repayments", precision = 15, scale = 2)
    private BigDecimal creditPrincipalRepayments;

    // Many turnover records belong to one header
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    @ToString.Exclude
    private MerchandiseTurnoverHeader header;

    public void setHeader(MerchandiseTurnoverHeader header) {
        this.header = header;
    }

    // Explicitly add getter for header if needed
    public MerchandiseTurnoverHeader getHeader() {
        return this.header;
    }
}
