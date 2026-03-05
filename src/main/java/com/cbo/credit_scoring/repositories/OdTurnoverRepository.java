package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.OdTurnover;
import com.cbo.credit_scoring.models.OdTurnoverHeader;
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
public interface OdTurnoverRepository extends JpaRepository<OdTurnover, Long> {

    // Find by header
    List<OdTurnover> findByHeader(OdTurnoverHeader header);

    List<OdTurnover> findByHeaderId(Long headerId);

    // Find by header and month
    Optional<OdTurnover> findByHeaderIdAndMonth(Long headerId, YearMonth month);

    boolean existsByHeaderIdAndMonth(Long headerId, YearMonth month);

    // Find by month
    List<OdTurnover> findByMonth(YearMonth month);

    List<OdTurnover> findByMonthBetween(YearMonth startMonth, YearMonth endMonth);

    // Find by utilization percentage ranges
    List<OdTurnover> findByUtilizationPercentageGreaterThanEqual(BigDecimal percentage);

    List<OdTurnover> findByUtilizationPercentageBetween(BigDecimal min, BigDecimal max);

    // Aggregation queries
    @Query("SELECT COALESCE(SUM(t.totalTurnoverCredit), 0) FROM OdTurnover t WHERE t.header.id = :headerId")
    BigDecimal sumTotalCreditByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(SUM(t.totalTurnoverDebit), 0) FROM OdTurnover t WHERE t.header.id = :headerId")
    BigDecimal sumTotalDebitByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(AVG(t.monthlyCreditAverage), 0) FROM OdTurnover t WHERE t.header.id = :headerId")
    BigDecimal averageMonthlyCreditByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(AVG(t.utilizationPercentage), 0) FROM OdTurnover t WHERE t.header.id = :headerId")
    BigDecimal averageUtilizationByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(SUM(t.numberOfCreditEntries), 0) FROM OdTurnover t WHERE t.header.id = :headerId")
    Integer sumNumberOfCreditEntriesByHeaderId(@Param("headerId") Long headerId);

    // Monthly summary
    @Query("SELECT t.month, t.totalTurnoverCredit, t.totalTurnoverDebit, " +
            "t.numberOfCreditEntries, t.monthlyCreditAverage, t.utilizationPercentage " +
            "FROM OdTurnover t WHERE t.header.id = :headerId ORDER BY t.month")
    List<Object[]> getMonthlyDataByHeaderId(@Param("headerId") Long headerId);

    // Find months with high utilization
    @Query("SELECT t FROM OdTurnover t WHERE t.header.id = :headerId " +
            "AND t.utilizationPercentage > :threshold ORDER BY t.utilizationPercentage DESC")
    List<OdTurnover> findHighUtilizationMonths(
            @Param("headerId") Long headerId,
            @Param("threshold") BigDecimal threshold);

    // Get latest turnover record
    @Query("SELECT t FROM OdTurnover t WHERE t.header.id = :headerId ORDER BY t.month DESC LIMIT 1")
    Optional<OdTurnover> findLatestByHeaderId(@Param("headerId") Long headerId);

    // Count records
    long countByHeaderId(Long headerId);

    @Query("SELECT COUNT(t) FROM OdTurnover t WHERE t.header.id = :headerId AND t.utilizationPercentage > :threshold")
    long countHighUtilizationMonths(@Param("headerId") Long headerId, @Param("threshold") BigDecimal threshold);

    // Delete operations
    @Modifying
    @Query("DELETE FROM OdTurnover t WHERE t.header.id = :headerId")
    void deleteByHeaderId(@Param("headerId") Long headerId);
}