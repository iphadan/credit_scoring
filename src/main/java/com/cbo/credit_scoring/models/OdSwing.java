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

@Entity
@Table(name = "od_swing")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "header")
public class OdSwing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "swing_month")
    private YearMonth month;

    @Column(name = "highest_utilization", precision = 15, scale = 2)
    private BigDecimal highestUtilization;

    @Column(name = "date_high")
    private LocalDate dateHigh;

    @Column(name = "lowest_utilization", precision = 15, scale = 2)
    private BigDecimal lowestUtilization;

    @Column(name = "date_low")
    private LocalDate dateLow;

    @Column(name = "utilization_percentage", precision = 5, scale = 2)
    private BigDecimal utilizationPercentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    @ToString.Exclude
    private OdSwingHeader header;

    // Business logic to validate dates
    @PrePersist
    @PreUpdate
    public void validateDates() {
        if (dateHigh != null && dateLow != null && dateHigh.isBefore(dateLow)) {
            throw new IllegalArgumentException("Date high cannot be before date low");
        }
    }

    // Business logic to calculate swing range
    public BigDecimal calculateSwingRange() {
        if (highestUtilization != null && lowestUtilization != null) {
            return highestUtilization.subtract(lowestUtilization);
        }
        return BigDecimal.ZERO;
    }

    // Business logic to check if utilization exceeds limit
    public boolean isExceedingLimit(BigDecimal sanctionLimit) {
        return utilizationPercentage != null &&
                sanctionLimit != null &&
                utilizationPercentage.compareTo(sanctionLimit) > 0;
    }
}