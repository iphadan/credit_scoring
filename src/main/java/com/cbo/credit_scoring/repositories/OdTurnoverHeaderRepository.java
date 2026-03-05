package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.OdTurnoverHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OdTurnoverHeaderRepository extends JpaRepository<OdTurnoverHeader, Long> {

    // Find by caseId
    Optional<OdTurnoverHeader> findByCaseId(String caseId);

    // Find by account number
    Optional<OdTurnoverHeader> findByAccountNumber(String accountNumber);

    // Find by account holder name
    List<OdTurnoverHeader> findByAccountHolderContainingIgnoreCase(String accountHolder);

    // Find by industry
    List<OdTurnoverHeader> findByIndustry(String industry);

    // Find by facility type
    List<OdTurnoverHeader> findByFacilityType(String facilityType);

    // Find by status
    List<OdTurnoverHeader> findByStatus(String status);

    // Find by date ranges
    List<OdTurnoverHeader> findByApprovedDateBetween(LocalDate startDate, LocalDate endDate);

    List<OdTurnoverHeader> findByReportDate(LocalDate reportDate);

    List<OdTurnoverHeader> findByReportDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by sanction limit ranges
    List<OdTurnoverHeader> findBySanctionLimitGreaterThanEqual(BigDecimal limit);

    List<OdTurnoverHeader> findBySanctionLimitBetween(BigDecimal min, BigDecimal max);

    // Custom search
    @Query("SELECT h FROM OdTurnoverHeader h WHERE " +
            "LOWER(h.accountHolder) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "h.accountNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
            "h.caseId LIKE CONCAT('%', :searchTerm, '%')")
    List<OdTurnoverHeader> searchByAnyField(@Param("searchTerm") String searchTerm);

    // Check existence
    boolean existsByCaseId(String caseId);

    boolean existsByAccountNumber(String accountNumber);

    // Find active accounts
    List<OdTurnoverHeader> findByStatusIgnoreCase(String status);

    // Find headers with turnover records
    @Query("SELECT DISTINCT h FROM OdTurnoverHeader h LEFT JOIN FETCH h.turnoverRecords WHERE h.id = :id")
    Optional<OdTurnoverHeader> findByIdWithTurnoverRecords(@Param("id") Long id);

    @Query("SELECT DISTINCT h FROM OdTurnoverHeader h LEFT JOIN FETCH h.turnoverRecords WHERE h.caseId = :caseId")
    Optional<OdTurnoverHeader> findByCaseIdWithTurnoverRecords(@Param("caseId") String caseId);
}