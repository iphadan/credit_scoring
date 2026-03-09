package com.cbo.credit_scoring.models;

import com.cbo.credit_scoring.models.enums.CollateralType;
import com.cbo.credit_scoring.models.enums.ValuationMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "collateral")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collateral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private String caseId;  // From frontend, links to Case

    @Column(name = "sr_no")
    private Integer srNo;  // Serial number within case

    @Enumerated(EnumType.STRING)
    @Column(name = "collateral_type", nullable = false)
    private CollateralType collateralType;

    @Column(name = "year_of_manufacturing")
    private Integer yearOfManufacturing;

    // Age is calculated, not stored

    @Column(name = "value", precision = 15, scale = 2)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(name = "valuation_method")
    private ValuationMethod valuationMethod;

    // mandatory_method, discount_rate, net_value are calculated, not stored

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