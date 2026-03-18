package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.OdSwingHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OdSwingHeaderRepository extends JpaRepository<OdSwingHeader, Long> {
    // Add these methods to existing OdSwingHeaderRepository.java

    /**
     * Find all headers by caseId (for cases with multiple versions)
     */
    List<OdSwingHeader> findAllByCaseId(String caseId);

    /**
     * Get all unique caseIds from the table
     */
    @Query("SELECT DISTINCT h.caseId FROM OdSwingHeader h WHERE h.caseId IS NOT NULL")
    List<String> findAllCaseIds();

    /**
     * Check if any records exist for a caseId
     */

    // Find by caseId
    Optional<OdSwingHeader> findByCaseId(String caseId);

    // Find by account number
    Optional<OdSwingHeader> findByAccountNumber(String accountNumber);

    // Find by account holder name
    List<OdSwingHeader> findByAccountHolderContainingIgnoreCase(String accountHolder);

    // Find by industry
    List<OdSwingHeader> findByIndustry(String industry);

    // Find by facility type
    List<OdSwingHeader> findByFacilityType(String facilityType);

    // Find by status
    List<OdSwingHeader> findByStatus(String status);

    // Find by date ranges
    List<OdSwingHeader> findByApprovedDateBetween(LocalDate startDate, LocalDate endDate);

    List<OdSwingHeader> findByReportDate(LocalDate reportDate);

    List<OdSwingHeader> findByReportDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by sanction limit ranges
    List<OdSwingHeader> findBySanctionLimitGreaterThanEqual(BigDecimal limit);

    List<OdSwingHeader> findBySanctionLimitBetween(BigDecimal min, BigDecimal max);

    // Custom search
    @Query("SELECT h FROM OdSwingHeader h WHERE " +
            "LOWER(h.accountHolder) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "h.accountNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
            "h.caseId LIKE CONCAT('%', :searchTerm, '%')")
    List<OdSwingHeader> searchByAnyField(@Param("searchTerm") String searchTerm);

    // Check existence
    boolean existsByCaseId(String caseId);

    boolean existsByAccountNumber(String accountNumber);

    // Find active accounts
    List<OdSwingHeader> findByStatusIgnoreCase(String status);

    // Find headers with swing records
    @Query("SELECT DISTINCT h FROM OdSwingHeader h LEFT JOIN FETCH h.swingRecords WHERE h.id = :id")
    Optional<OdSwingHeader> findByIdWithSwingRecords(@Param("id") Long id);

    @Query("SELECT DISTINCT h FROM OdSwingHeader h LEFT JOIN FETCH h.swingRecords WHERE h.caseId = :caseId")
    Optional<OdSwingHeader> findByCaseIdWithSwingRecords(@Param("caseId") String caseId);
}