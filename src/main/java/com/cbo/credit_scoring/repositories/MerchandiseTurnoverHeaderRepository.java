package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.MerchandiseTurnoverHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MerchandiseTurnoverHeaderRepository extends JpaRepository<MerchandiseTurnoverHeader, Long> {

    boolean existsByCaseId(String caseId);


    Optional<MerchandiseTurnoverHeader> findByCaseId(String caseId);

    // Basic search methods
    List<MerchandiseTurnoverHeader> findByCustomerNameContainingIgnoreCase(String customerName);

    List<MerchandiseTurnoverHeader> findByAccountNumber(String accountNumber);

    List<MerchandiseTurnoverHeader> findByAccountNumberContaining(String accountNumber);

    List<MerchandiseTurnoverHeader> findByTypeOfFacility(String typeOfFacility);

    List<MerchandiseTurnoverHeader> findByIndustryType(String industryType);

    // Date range queries
    List<MerchandiseTurnoverHeader> findByDateApprovedBetween(LocalDate startDate, LocalDate endDate);

    List<MerchandiseTurnoverHeader> findByReportingDate(LocalDate reportingDate);

    List<MerchandiseTurnoverHeader> findByReportingDateBetween(LocalDate startDate, LocalDate endDate);

    // Amount range queries
    List<MerchandiseTurnoverHeader> findByApprovedAmountBetween(Double minAmount, Double maxAmount);

    List<MerchandiseTurnoverHeader> findByApprovedAmountGreaterThanEqual(Double amount);

    List<MerchandiseTurnoverHeader> findByApprovedAmountLessThanEqual(Double amount);

    // Combined queries
    List<MerchandiseTurnoverHeader> findByCustomerNameAndDateApprovedBetween(
            String customerName, LocalDate startDate, LocalDate endDate);

    List<MerchandiseTurnoverHeader> findByTypeOfFacilityAndIndustryType(
            String typeOfFacility, String industryType);

    // Check existence
    boolean existsByAccountNumber(String accountNumber);

    // Custom queries with JPQL
    @Query("SELECT h FROM MerchandiseTurnoverHeader h WHERE h.customerName LIKE %:name% OR h.accountNumber LIKE %:account%")
    List<MerchandiseTurnoverHeader> searchByCustomerNameOrAccountNumber(
            @Param("name") String customerName,
            @Param("account") String accountNumber);

    @Query("SELECT h FROM MerchandiseTurnoverHeader h WHERE SIZE(h.turnoverRecords) > :count")
    List<MerchandiseTurnoverHeader> findHeadersWithMoreThanNTurnoverRecords(@Param("count") int count);

    @Query("SELECT h FROM MerchandiseTurnoverHeader h WHERE h.dateApproved >= :date")
    List<MerchandiseTurnoverHeader> findHeadersApprovedAfter(@Param("date") LocalDate date);

    // Aggregation queries
    @Query("SELECT COUNT(h) FROM MerchandiseTurnoverHeader h WHERE h.reportingDate = :date")
    Long countByReportingDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(h.approvedAmount) FROM MerchandiseTurnoverHeader h WHERE h.dateApproved BETWEEN :startDate AND :endDate")
    Double sumApprovedAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Native query example (if needed)
    @Query(value = "SELECT * FROM merchandise_turnover_header WHERE YEAR(date_approved) = :year",
            nativeQuery = true)
    List<MerchandiseTurnoverHeader> findHeadersApprovedInYear(@Param("year") int year);

    // Find headers with their turnover records (eager loading for specific use cases)
    @Query("SELECT DISTINCT h FROM MerchandiseTurnoverHeader h LEFT JOIN FETCH h.turnoverRecords WHERE h.id = :id")
    Optional<MerchandiseTurnoverHeader> findByIdWithTurnoverRecords(@Param("id") Long id);

    // Find headers that have turnover records in a specific month
    @Query("SELECT DISTINCT h FROM MerchandiseTurnoverHeader h JOIN h.turnoverRecords t WHERE t.month = :month")
    List<MerchandiseTurnoverHeader> findHeadersWithTurnoverInMonth(@Param("month") Object month);
}