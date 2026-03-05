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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pre_shipment_turnover_header")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "turnoverRecords")
public class PreShipmentTurnoverHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private String caseId; // will be added upon creation and will be used to uniquely identify the record and related table rows

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "type_of_facility")
    private String typeOfFacility;

    @Column(name = "industry_type")
    private String industryType;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "date_approved")
    private LocalDate dateApproved;

    @Column(name = "reporting_date")
    private LocalDate reportingDate;

    // One header can have many turnover records
    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<PreShipmentTurnover> turnoverRecords = new ArrayList<>();

    // Helper methods to manage bidirectional relationship
    public void addTurnoverRecord(PreShipmentTurnover turnover) {
        turnoverRecords.add(turnover);
        turnover.setHeader(this);
    }

    public void removeTurnoverRecord(PreShipmentTurnover turnover) {
        turnoverRecords.remove(turnover);
        turnover.setHeader(null);
    }
}
