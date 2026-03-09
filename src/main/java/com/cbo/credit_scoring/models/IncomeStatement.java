package com.cbo.credit_scoring.models;

import com.cbo.credit_scoring.models.FinancialStatement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "income_statements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_statement_id", nullable = false)
    private FinancialStatement financialStatement;

    private LocalDate periodDate;
    private Boolean isAudited;
    private Integer periodOrder;

    // Income Statement Items
    @Column(precision = 15, scale = 2)
    private BigDecimal sales;

    @Column(name = "cost_of_goods_sold", precision = 15, scale = 2)
    private BigDecimal costOfGoodsSold;

    @Column(name = "operating_expenses", precision = 15, scale = 2)
    private BigDecimal operatingExpenses;

    @Column(name = "depreciation_expense", precision = 15, scale = 2)
    private BigDecimal depreciationExpense;

    @Column(name = "interest_expense", precision = 15, scale = 2)
    private BigDecimal interestExpense;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate; // Default 35%

    // Calculated fields
    @Transient
    public BigDecimal getGrossProfit() {
        return (sales != null ? sales : BigDecimal.ZERO)
                .subtract(costOfGoodsSold != null ? costOfGoodsSold : BigDecimal.ZERO);
    }

    @Transient
    public BigDecimal getProfitBeforeInterestTaxDepreciation() {
        return getGrossProfit()
                .subtract(operatingExpenses != null ? operatingExpenses : BigDecimal.ZERO);
    }

    @Transient
    public BigDecimal getProfitBeforeInterestAndTax() {
        return getProfitBeforeInterestTaxDepreciation()
                .subtract(depreciationExpense != null ? depreciationExpense : BigDecimal.ZERO);
    }

    @Transient
    public BigDecimal getProfitBeforeTax() {
        return getProfitBeforeInterestAndTax()
                .subtract(interestExpense != null ? interestExpense : BigDecimal.ZERO);
    }

    @Transient
    public BigDecimal getIncomeTax() {
        BigDecimal pbt = getProfitBeforeTax();
        BigDecimal rate = taxRate != null ? taxRate : new BigDecimal("35");
        return pbt.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    @Transient
    public BigDecimal getNetIncome() {
        return getProfitBeforeTax().subtract(getIncomeTax());
    }
}