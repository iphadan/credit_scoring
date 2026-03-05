package com.cbo.credit_scoring.models;

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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "od_swing_header")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "swingRecords")
public class OdSwingHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "case_id", unique = true)
    private String caseId; // Unique identifier for the record

    @Column(name = "account_holder", nullable = false)
    private String accountHolder;

    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @Column(name = "industry")
    private String industry;

    @Column(name = "sanction_limit", precision = 15, scale = 2)
    private BigDecimal sanctionLimit;

    @Column(name = "approved_date")
    private LocalDate approvedDate;

    @Column(name = "facility_type")
    private String facilityType;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Column(name = "status")
    private String status; // ACTIVE, CLOSED, SUSPENDED, etc.

    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<OdSwing> swingRecords = new ArrayList<>();

    // Helper methods
    public void addSwingRecord(OdSwing swing) {
        swingRecords.add(swing);
        swing.setHeader(this);
    }

    public void removeSwingRecord(OdSwing swing) {
        swingRecords.remove(swing);
        swing.setHeader(null);
    }
}