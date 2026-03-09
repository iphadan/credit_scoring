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
@Table(name = "balance_sheets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "financial_statement_id", nullable = false)
    private FinancialStatement financialStatement;

    private LocalDate periodDate;
    private Boolean isAudited;
    private Integer periodOrder; // 0,1,2,3 for the 4 periods

    // Non-current Assets
    @Column(name = "property_plant_equipment", precision = 15, scale = 2)
    private BigDecimal propertyPlantEquipment;

    // Current Assets
    @Column(precision = 15, scale = 2)
    private BigDecimal stocks;

    @Column(name = "trade_other_receivables", precision = 15, scale = 2)
    private BigDecimal tradeOtherReceivables;

    @Column(name = "cash_on_hand_bank", precision = 15, scale = 2)
    private BigDecimal cashOnHandBank;

    // Current Liabilities
    @Column(name = "trade_other_payables", precision = 15, scale = 2)
    private BigDecimal tradeOtherPayables;

    @Column(name = "other_payables", precision = 15, scale = 2)
    private BigDecimal otherPayables;

    @Column(name = "shareholder_account", precision = 15, scale = 2)
    private BigDecimal shareholderAccount;

    @Column(name = "profit_tax_payable", precision = 15, scale = 2)
    private BigDecimal profitTaxPayable;

    // Long Term Liabilities
    @Column(name = "bank_loan", precision = 15, scale = 2)
    private BigDecimal bankLoan;

    @Column(name = "lease_payable", precision = 15, scale = 2)
    private BigDecimal leasePayable;

    // Capital
    @Column(precision = 15, scale = 2)
    private BigDecimal capital;

    @Column(precision = 15, scale = 2)
    private BigDecimal reserve;

    @Column(name = "retained_earnings", precision = 15, scale = 2)
    private BigDecimal retainedEarnings;

    // Calculated fields (not stored)
    @Transient
    public BigDecimal getTotalNonCurrentAssets() {
        return propertyPlantEquipment != null ? propertyPlantEquipment : BigDecimal.ZERO;
    }

    @Transient
    public BigDecimal getTotalCurrentAssets() {
        return (stocks != null ? stocks : BigDecimal.ZERO)
                .add(tradeOtherReceivables != null ? tradeOtherReceivables : BigDecimal.ZERO)
                .add(cashOnHandBank != null ? cashOnHandBank : BigDecimal.ZERO);
    }

    @Transient
    public BigDecimal getTotalAssets() {
        return getTotalNonCurrentAssets().add(getTotalCurrentAssets());
    }

    @Transient
    public BigDecimal getTotalCurrentLiabilities() {
        return (tradeOtherPayables != null ? tradeOtherPayables : BigDecimal.ZERO)
                .add(otherPayables != null ? otherPayables : BigDecimal.ZERO)
                .add(shareholderAccount != null ? shareholderAccount : BigDecimal.ZERO)
                .add(profitTaxPayable != null ? profitTaxPayable : BigDecimal.ZERO);
    }

    @Transient
    public BigDecimal getTotalLongTermLiabilities() {
        return (bankLoan != null ? bankLoan : BigDecimal.ZERO)
                .add(leasePayable != null ? leasePayable : BigDecimal.ZERO);
    }

    @Transient
    public BigDecimal getTotalLiabilities() {
        return getTotalCurrentLiabilities().add(getTotalLongTermLiabilities());
    }

    @Transient
    public BigDecimal getTotalCapital() {
        return (capital != null ? capital : BigDecimal.ZERO)
                .add(reserve != null ? reserve : BigDecimal.ZERO)
                .add(retainedEarnings != null ? retainedEarnings : BigDecimal.ZERO);
    }

    @Transient
    public BigDecimal getTotalLiabilitiesAndCapital() {
        return getTotalLiabilities().add(getTotalCapital());
    }

    @Transient
    public BigDecimal getNetWorkingCapital() {
        return getTotalCurrentAssets().subtract(getTotalCurrentLiabilities());
    }

    @Transient
    public BigDecimal getTangibleNetWorth() {
        return getTotalAssets().subtract(getTotalLiabilities());
    }
}