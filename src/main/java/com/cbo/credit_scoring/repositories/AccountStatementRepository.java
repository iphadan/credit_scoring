package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.AccountStatement;
import com.cbo.credit_scoring.models.AccountStatementHeader;
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
public interface AccountStatementRepository extends JpaRepository<AccountStatement, Long> {

    // Find by header
    List<AccountStatement> findByHeader(AccountStatementHeader header);

    List<AccountStatement> findByHeaderId(Long headerId);

    List<AccountStatement> findByHeaderIdOrderByMonthAsc(Long headerId);

    List<AccountStatement> findByHeaderIdOrderByMonthDesc(Long headerId);

    // Find by header and month
    Optional<AccountStatement> findByHeaderIdAndMonth(Long headerId, YearMonth month);

    boolean existsByHeaderIdAndMonth(Long headerId, YearMonth month);

    // Find by month
    List<AccountStatement> findByMonth(YearMonth month);

    List<AccountStatement> findByMonthBetween(YearMonth startMonth, YearMonth endMonth);

    // Find by utilization ranges
    List<AccountStatement> findByUtilizationPercentageGreaterThanEqual(BigDecimal percentage);

    List<AccountStatement> findByUtilizationPercentageBetween(BigDecimal min, BigDecimal max);

    // Find by number of credit entries
    List<AccountStatement> findByNumberOfCreditEntriesGreaterThanEqual(Integer count);

    // Aggregation queries
    @Query("SELECT COALESCE(SUM(s.totalTurnoverCredit), 0) FROM AccountStatement s WHERE s.header.id = :headerId")
    BigDecimal sumTotalCreditByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(SUM(s.totalTurnoverDebit), 0) FROM AccountStatement s WHERE s.header.id = :headerId")
    BigDecimal sumTotalDebitByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(AVG(s.monthlyCreditAverage), 0) FROM AccountStatement s WHERE s.header.id = :headerId")
    BigDecimal averageMonthlyCreditByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(AVG(s.utilizationPercentage), 0) FROM AccountStatement s WHERE s.header.id = :headerId")
    BigDecimal averageUtilizationByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(MAX(s.utilizationPercentage), 0) FROM AccountStatement s WHERE s.header.id = :headerId")
    BigDecimal maxUtilizationByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(MIN(s.utilizationPercentage), 0) FROM AccountStatement s WHERE s.header.id = :headerId")
    BigDecimal minUtilizationByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(SUM(s.numberOfCreditEntries), 0) FROM AccountStatement s WHERE s.header.id = :headerId")
    Integer sumNumberOfCreditEntriesByHeaderId(@Param("headerId") Long headerId);

    // Find month with highest utilization
    @Query("SELECT s FROM AccountStatement s WHERE s.header.id = :headerId ORDER BY s.utilizationPercentage DESC LIMIT 1")
    Optional<AccountStatement> findMonthWithHighestUtilization(@Param("headerId") Long headerId);

    // Find month with lowest utilization
    @Query("SELECT s FROM AccountStatement s WHERE s.header.id = :headerId ORDER BY s.utilizationPercentage ASC LIMIT 1")
    Optional<AccountStatement> findMonthWithLowestUtilization(@Param("headerId") Long headerId);

    // Find month with highest credit
    @Query("SELECT s FROM AccountStatement s WHERE s.header.id = :headerId ORDER BY s.totalTurnoverCredit DESC LIMIT 1")
    Optional<AccountStatement> findMonthWithHighestCredit(@Param("headerId") Long headerId);

    // Find month with highest debit
    @Query("SELECT s FROM AccountStatement s WHERE s.header.id = :headerId ORDER BY s.totalTurnoverDebit DESC LIMIT 1")
    Optional<AccountStatement> findMonthWithHighestDebit(@Param("headerId") Long headerId);

    // Count months exceeding threshold
    @Query("SELECT COUNT(s) FROM AccountStatement s WHERE s.header.id = :headerId AND s.utilizationPercentage > :threshold")
    long countMonthsExceedingThreshold(@Param("headerId") Long headerId, @Param("threshold") BigDecimal threshold);

    // Get latest statement record
    @Query("SELECT s FROM AccountStatement s WHERE s.header.id = :headerId ORDER BY s.month DESC LIMIT 1")
    Optional<AccountStatement> findLatestByHeaderId(@Param("headerId") Long headerId);

    // Get oldest statement record
    @Query("SELECT s FROM AccountStatement s WHERE s.header.id = :headerId ORDER BY s.month ASC LIMIT 1")
    Optional<AccountStatement> findOldestByHeaderId(@Param("headerId") Long headerId);

    // Count records
    long countByHeaderId(Long headerId);

    @Query("SELECT COUNT(s) FROM AccountStatement s WHERE s.header.id = :headerId AND s.totalTurnoverCredit > 0")
    long countActiveMonthsByHeaderId(@Param("headerId") Long headerId);

    // Delete operations
    @Modifying
    @Query("DELETE FROM AccountStatement s WHERE s.header.id = :headerId")
    void deleteByHeaderId(@Param("headerId") Long headerId);
}