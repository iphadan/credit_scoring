package com.cbo.credit_scoring.models;

import com.cbo.credit_scoring.utils.YearMonthConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(name = "pre_shipment_turnover")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "header")
public class PreShipmentTurnover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Convert(converter = YearMonthConverter.class)
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
    private PreShipmentTurnoverHeader header;

    public void setHeader(PreShipmentTurnoverHeader header) {
        this.header = header;
    }

    // Explicitly add getter for header if needed
    public PreShipmentTurnoverHeader getHeader() {
        return this.header;
}}