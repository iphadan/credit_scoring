package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.MerchandiseTurnover;
import com.cbo.credit_scoring.models.MerchandiseTurnoverHeader;
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
public interface MerchandiseTurnoverRepository extends JpaRepository<MerchandiseTurnover, Long> {

    // Basic find by header
    List<MerchandiseTurnover> findByHeader(MerchandiseTurnoverHeader header);

    List<MerchandiseTurnover> findByHeaderId(Long headerId);

    // Find by month
    List<MerchandiseTurnover> findByMonth(YearMonth month);

    List<MerchandiseTurnover> findByMonthBetween(YearMonth startMonth, YearMonth endMonth);

    // Find by header and month
    Optional<MerchandiseTurnover> findByHeaderIdAndMonth(Long headerId, YearMonth month);

    boolean existsByHeaderIdAndMonth(Long headerId, YearMonth month);

    // Find by amount ranges
    List<MerchandiseTurnover> findByDebitDisbursementsGreaterThanEqual(BigDecimal amount);

    List<MerchandiseTurnover> findByCreditPrincipalRepaymentsGreaterThanEqual(BigDecimal amount);

    List<MerchandiseTurnover> findByDebitDisbursementsBetween(BigDecimal min, BigDecimal max);

    List<MerchandiseTurnover> findByCreditPrincipalRepaymentsBetween(BigDecimal min, BigDecimal max);

    // Combined queries
    List<MerchandiseTurnover> findByHeaderIdAndDebitDisbursementsGreaterThan(
            Long headerId, BigDecimal amount);

    List<MerchandiseTurnover> findByHeaderIdAndCreditPrincipalRepaymentsGreaterThan(
            Long headerId, BigDecimal amount);

    // Aggregation queries
    @Query("SELECT SUM(t.debitDisbursements) FROM MerchandiseTurnover t WHERE t.header.id = :headerId")
    BigDecimal sumDebitByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT SUM(t.creditPrincipalRepayments) FROM MerchandiseTurnover t WHERE t.header.id = :headerId")
    BigDecimal sumCreditByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT SUM(t.debitDisbursements) - SUM(t.creditPrincipalRepayments) " +
            "FROM MerchandiseTurnover t WHERE t.header.id = :headerId")
    BigDecimal calculateNetTurnoverByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT AVG(t.debitDisbursements) FROM MerchandiseTurnover t WHERE t.header.id = :headerId")
    BigDecimal averageDebitByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT MAX(t.debitDisbursements) FROM MerchandiseTurnover t WHERE t.header.id = :headerId")
    BigDecimal maxDebitByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT MIN(t.debitDisbursements) FROM MerchandiseTurnover t WHERE t.header.id = :headerId")
    BigDecimal minDebitByHeaderId(@Param("headerId") Long headerId);

    // Monthly summaries
    @Query("SELECT t.month, SUM(t.debitDisbursements), SUM(t.creditPrincipalRepayments) " +
            "FROM MerchandiseTurnover t " +
            "WHERE t.header.id = :headerId " +
            "GROUP BY t.month " +
            "ORDER BY t.month")
    List<Object[]> getMonthlySummaryByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT t.month, SUM(t.debitDisbursements), SUM(t.creditPrincipalRepayments) " +
            "FROM MerchandiseTurnover t " +
            "WHERE t.month BETWEEN :startMonth AND :endMonth " +
            "GROUP BY t.month " +
            "ORDER BY t.month")
    List<Object[]> getMonthlySummaryByDateRange(
            @Param("startMonth") YearMonth startMonth,
            @Param("endMonth") YearMonth endMonth);

    // Delete operations
    @Modifying
    @Query("DELETE FROM MerchandiseTurnover t WHERE t.header.id = :headerId")
    void deleteByHeaderId(@Param("headerId") Long headerId);

    // Count operations
    long countByHeaderId(Long headerId);

    @Query("SELECT COUNT(t) FROM MerchandiseTurnover t WHERE t.month = :month")
    long countByMonth(@Param("month") YearMonth month);

    // Find top records
    List<MerchandiseTurnover> findTop10ByOrderByDebitDisbursementsDesc();

    List<MerchandiseTurnover> findTop10ByOrderByCreditPrincipalRepaymentsDesc();

    // Custom JPQL queries with joins
    @Query("SELECT t FROM MerchandiseTurnover t JOIN FETCH t.header h WHERE h.customerName LIKE %:customerName%")
    List<MerchandiseTurnover> findByCustomerNameWithHeader(@Param("customerName") String customerName);

    @Query("SELECT t FROM MerchandiseTurnover t WHERE t.debitDisbursements > " +
            "(SELECT AVG(t2.debitDisbursements) FROM MerchandiseTurnover t2 WHERE t2.header.id = t.header.id)")
    List<MerchandiseTurnover> findTurnoversAboveAverageDebit();

    // Native queries for complex reporting
    @Query(value = "SELECT header_id, " +
            "SUM(debit_disbursements) as total_debit, " +
            "SUM(credit_principal_repayments) as total_credit, " +
            "SUM(debit_disbursements) - SUM(credit_principal_repayments) as net_turnover " +
            "FROM merchandise_turnover " +
            "WHERE turnover_month BETWEEN :startMonth AND :endMonth " +
            "GROUP BY header_id " +
            "HAVING net_turnover > :minNetAmount",
            nativeQuery = true)
    List<Object[]> getHeaderSummariesWithNetTurnoverAbove(
            @Param("startMonth") String startMonth,
            @Param("endMonth") String endMonth,
            @Param("minNetAmount") BigDecimal minNetAmount);

    // Batch update operations
    @Modifying
    @Query("UPDATE MerchandiseTurnover t SET t.debitDisbursements = :debitAmount " +
            "WHERE t.header.id = :headerId AND t.month = :month")
    int updateDebitByHeaderIdAndMonth(
            @Param("headerId") Long headerId,
            @Param("month") YearMonth month,
            @Param("debitAmount") BigDecimal debitAmount);

    // Find headers with missing months (for validation)
    @Query("SELECT DISTINCT t.header.id FROM MerchandiseTurnover t " +
            "WHERE t.header.id NOT IN " +
            "(SELECT DISTINCT t2.header.id FROM MerchandiseTurnover t2 WHERE t2.month = :month)")
    List<Long> findHeaderIdsWithoutTurnoverForMonth(@Param("month") YearMonth month);
}