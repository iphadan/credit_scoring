package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.OdSwing;
import com.cbo.credit_scoring.models.OdSwingHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface OdSwingRepository extends JpaRepository<OdSwing, Long> {

    // Find by header
    List<OdSwing> findByHeader(OdSwingHeader header);

    List<OdSwing> findByHeaderId(Long headerId);

    // Find by header and month
    Optional<OdSwing> findByHeaderIdAndMonth(Long headerId, YearMonth month);

    boolean existsByHeaderIdAndMonth(Long headerId, YearMonth month);

    // Find by month
    List<OdSwing> findByMonth(YearMonth month);

    List<OdSwing> findByMonthBetween(YearMonth startMonth, YearMonth endMonth);

    // Find by date ranges
    List<OdSwing> findByDateHighBetween(LocalDate startDate, LocalDate endDate);

    List<OdSwing> findByDateLowBetween(LocalDate startDate, LocalDate endDate);

    // Find by utilization ranges
    List<OdSwing> findByUtilizationPercentageGreaterThanEqual(BigDecimal percentage);

    List<OdSwing> findByUtilizationPercentageBetween(BigDecimal min, BigDecimal max);

    List<OdSwing> findByHighestUtilizationGreaterThanEqual(BigDecimal amount);

    List<OdSwing> findByLowestUtilizationLessThanEqual(BigDecimal amount);

    // Aggregation queries
    @Query("SELECT COALESCE(AVG(s.utilizationPercentage), 0) FROM OdSwing s WHERE s.header.id = :headerId")
    BigDecimal averageUtilizationByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(AVG(s.highestUtilization), 0) FROM OdSwing s WHERE s.header.id = :headerId")
    BigDecimal averageHighestUtilizationByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(AVG(s.lowestUtilization), 0) FROM OdSwing s WHERE s.header.id = :headerId")
    BigDecimal averageLowestUtilizationByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(MAX(s.utilizationPercentage), 0) FROM OdSwing s WHERE s.header.id = :headerId")
    BigDecimal maxUtilizationByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(MIN(s.utilizationPercentage), 0) FROM OdSwing s WHERE s.header.id = :headerId")
    BigDecimal minUtilizationByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(MAX(s.highestUtilization - s.lowestUtilization), 0) FROM OdSwing s WHERE s.header.id = :headerId")
    BigDecimal maxSwingRangeByHeaderId(@Param("headerId") Long headerId);

    @Query("SELECT COALESCE(AVG(s.highestUtilization - s.lowestUtilization), 0) FROM OdSwing s WHERE s.header.id = :headerId")
    BigDecimal averageSwingRangeByHeaderId(@Param("headerId") Long headerId);

    // Find month with highest utilization
    @Query("SELECT s FROM OdSwing s WHERE s.header.id = :headerId ORDER BY s.utilizationPercentage DESC LIMIT 1")
    Optional<OdSwing> findMonthWithHighestUtilization(@Param("headerId") Long headerId);

    // Find month with lowest utilization
    @Query("SELECT s FROM OdSwing s WHERE s.header.id = :headerId ORDER BY s.utilizationPercentage ASC LIMIT 1")
    Optional<OdSwing> findMonthWithLowestUtilization(@Param("headerId") Long headerId);

    // Find month with widest swing range
    @Query("SELECT s FROM OdSwing s WHERE s.header.id = :headerId ORDER BY (s.highestUtilization - s.lowestUtilization) DESC LIMIT 1")
    Optional<OdSwing> findMonthWithWidestSwing(@Param("headerId") Long headerId);

    // Count months exceeding threshold
    @Query("SELECT COUNT(s) FROM OdSwing s WHERE s.header.id = :headerId AND s.utilizationPercentage > :threshold")
    long countMonthsExceedingThreshold(@Param("headerId") Long headerId, @Param("threshold") BigDecimal threshold);

    // Get monthly data ordered
    @Query("SELECT s FROM OdSwing s WHERE s.header.id = :headerId ORDER BY s.month")
    List<OdSwing> findByHeaderIdOrderByMonth(@Param("headerId") Long headerId);

    // Get latest swing record
    @Query("SELECT s FROM OdSwing s WHERE s.header.id = :headerId ORDER BY s.month DESC LIMIT 1")
    Optional<OdSwing> findLatestByHeaderId(@Param("headerId") Long headerId);

    // Count records
    long countByHeaderId(Long headerId);

    // Delete operations
    @Modifying
    @Query("DELETE FROM OdSwing s WHERE s.header.id = :headerId")
    void deleteByHeaderId(@Param("headerId") Long headerId);
}