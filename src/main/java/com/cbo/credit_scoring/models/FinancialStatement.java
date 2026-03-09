package com.cbo.credit_scoring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "financial_statements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case caseId; // Using case_ to avoid Java keyword conflict

    private String companyName;
    private String statementType; // 'BALANCE_SHEET', 'INCOME_STATEMENT', 'COMPREHENSIVE'
    private LocalDate reportingDate;
    private Integer version; // For tracking updates/revisions

    @OneToMany(mappedBy = "financialStatement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BalanceSheet> balanceSheets = new ArrayList<>();

    @OneToMany(mappedBy = "financialStatement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IncomeStatement> incomeStatements = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
}