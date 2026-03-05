package com.cbo.credit_scoring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

@Entity
@Table(name = "account_statement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "header")
public class AccountStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "statement_month")
    private YearMonth month;

    @Column(name = "total_turnover_credit", precision = 15, scale = 2)
    private BigDecimal totalTurnoverCredit;

    @Column(name = "total_turnover_debit", precision = 15, scale = 2)
    private BigDecimal totalTurnoverDebit;

    @Column(name = "number_of_credit_entries")
    private Integer numberOfCreditEntries;

    @Column(name = "monthly_credit_average", precision = 15, scale = 2)
    private BigDecimal monthlyCreditAverage;

    @Column(name = "utilization_percentage", precision = 5, scale = 2)
    private BigDecimal utilizationPercentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    @ToString.Exclude
    private AccountStatementHeader header;

    // Business logic to calculate monthly credit average
    @PrePersist
    @PreUpdate
    public void calculateMonthlyCreditAverage() {
        if (totalTurnoverCredit != null && numberOfCreditEntries != null && numberOfCreditEntries > 0) {
            this.monthlyCreditAverage = totalTurnoverCredit.divide(
                    BigDecimal.valueOf(numberOfCreditEntries), 2, RoundingMode.HALF_UP);
        } else {
            this.monthlyCreditAverage = BigDecimal.ZERO;
        }
    }

    // Business logic to calculate utilization percentage
    public void calculateUtilizationPercentage(BigDecimal sanctionLimit) {
        if (sanctionLimit != null && sanctionLimit.compareTo(BigDecimal.ZERO) > 0
                && totalTurnoverDebit != null) {
            this.utilizationPercentage = totalTurnoverDebit
                    .multiply(BigDecimal.valueOf(100))
                    .divide(sanctionLimit, 2, RoundingMode.HALF_UP);
        } else {
            this.utilizationPercentage = BigDecimal.ZERO;
        }
    }

    // Business logic to calculate net turnover
    public BigDecimal calculateNetTurnover() {
        if (totalTurnoverCredit != null && totalTurnoverDebit != null) {
            return totalTurnoverCredit.subtract(totalTurnoverDebit);
        }
        return BigDecimal.ZERO;
    }
}