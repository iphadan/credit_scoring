package com.cbo.credit_scoring.models;

import com.cbo.credit_scoring.models.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "credit_information")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private String caseId;  // From frontend, links to Case

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_type", nullable = false)
    private BankType bankType;  // COOP_BANK or OTHER_BANK

    @Column(name = "sr_no")
    private Integer srNo;  // Serial number within each bank type

    @Enumerated(EnumType.STRING)
    @Column(name = "exposure_type", nullable = false)
    private ExposureType exposureType;  // REVOLVING or NON_REVOLVING

    @Column(name = "existing_exposure")
    private String existingExposure;  // Description of the exposure

    @Column(name = "lending_bank")
    private String lendingBank;  // Name of the bank (for OTHER_BANK type)

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_product")
    private CreditProductType creditProduct;

    @Column(name = "date_granted")
    private LocalDate dateGranted;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "amount_granted", precision = 15, scale = 2)
    private BigDecimal amountGranted;

    @Column(name = "current_balance", precision = 15, scale = 2)
    private BigDecimal currentBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FacilityStatus status;

    @Column(name = "reporting_date")
    private LocalDate reportingDate;

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