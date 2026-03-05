package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.PreShipmentTurnover;
import com.cbo.credit_scoring.models.PreShipmentTurnoverHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface PreShipmentTurnoverRepository extends JpaRepository<PreShipmentTurnover, Long> {

    // Find by header
    List<PreShipmentTurnover> findByHeader(PreShipmentTurnoverHeader header);

    List<PreShipmentTurnover> findByHeaderId(Long headerId);

    // Find by header and month
    Optional<PreShipmentTurnover> findByHeaderIdAndMonth(Long headerId, YearMonth month);

    boolean existsByHeaderIdAndMonth(Long headerId, YearMonth month);

    // Find by month
    List<PreShipmentTurnover> findByMonth(YearMonth month);

    List<PreShipmentTurnover> findByMonthBetween(YearMonth startMonth, YearMonth endMonth);

    // Amount queries
    List<PreShipmentTurnover> findByDebitDisbursementsGreaterThanEqual(BigDecimal amount);

    List<PreShipmentTurnover> findByCreditPrincipalRepaymentsGreaterThanEqual(BigDecimal amount);

    // Aggregation queries
    @Query("SELECT COALESCE(SUM(t.debitDisbursements), 0) FROM PreShipmentTurnover t WHERE t.header.id = :headerId")
    BigDecimal sumDebitByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(SUM(t.creditPrincipalRepayments), 0) FROM PreShipmentTurnover t WHERE t.header.id = :headerId")
    BigDecimal sumCreditByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(SUM(t.debitDisbursements), 0) - COALESCE(SUM(t.creditPrincipalRepayments), 0) " +
            "FROM PreShipmentTurnover t WHERE t.header.id = :headerId")
    BigDecimal calculateNetTurnoverByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT t.month, COALESCE(SUM(t.debitDisbursements), 0), COALESCE(SUM(t.creditPrincipalRepayments), 0) " +
            "FROM PreShipmentTurnover t " +
            "WHERE t.header.id = :headerId " +
            "GROUP BY t.month " +
            "ORDER BY t.month")
    List<Object[]> getMonthlySummaryByHeaderId(@Param("headerId") Long headerId);

    // Count operations
    long countByHeaderId(Long headerId);

    // Delete operations
    @Modifying
    @Query("DELETE FROM PreShipmentTurnover t WHERE t.header.id = :headerId")
    void deleteByHeaderId(@Param("headerId") Long headerId);
}