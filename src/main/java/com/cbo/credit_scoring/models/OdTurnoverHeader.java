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
@Table(name = "od_turnover_header")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "turnoverRecords")
public class OdTurnoverHeader {

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
    private List<OdTurnover> turnoverRecords = new ArrayList<>();

    // Helper methods
    public void addTurnoverRecord(OdTurnover turnover) {
        turnoverRecords.add(turnover);
        turnover.setHeader(this);
    }

    public void removeTurnoverRecord(OdTurnover turnover) {
        turnoverRecords.remove(turnover);
        turnover.setHeader(null);
    }
}