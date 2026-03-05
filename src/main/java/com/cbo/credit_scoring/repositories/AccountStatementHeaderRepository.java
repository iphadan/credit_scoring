package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.AccountStatementHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountStatementHeaderRepository extends JpaRepository<AccountStatementHeader, Long> {

    // Find by caseId
    Optional<AccountStatementHeader> findByCaseId(String caseId);

    // Find by account number
    Optional<AccountStatementHeader> findByAccountNumber(String accountNumber);

    // Find by account holder name
    List<AccountStatementHeader> findByAccountHolderContainingIgnoreCase(String accountHolder);

    // Find by industry
    List<AccountStatementHeader> findByIndustry(String industry);

    // Find by facility type
    List<AccountStatementHeader> findByFacilityType(String facilityType);

    // Find by status
    List<AccountStatementHeader> findByStatus(String status);

    // Find by date ranges
    List<AccountStatementHeader> findByApprovedDateBetween(LocalDate startDate, LocalDate endDate);

    List<AccountStatementHeader> findByDateAccountOpenedBetween(LocalDate startDate, LocalDate endDate);

    List<AccountStatementHeader> findByReportDate(LocalDate reportDate);

    List<AccountStatementHeader> findByReportDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by sanction limit ranges
    List<AccountStatementHeader> findBySanctionLimitGreaterThanEqual(BigDecimal limit);

    List<AccountStatementHeader> findBySanctionLimitBetween(BigDecimal min, BigDecimal max);

    // Custom search
    @Query("SELECT h FROM AccountStatementHeader h WHERE " +
            "LOWER(h.accountHolder) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "h.accountNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
            "h.caseId LIKE CONCAT('%', :searchTerm, '%')")
    List<AccountStatementHeader> searchByAnyField(@Param("searchTerm") String searchTerm);

    // Check existence
    boolean existsByCaseId(String caseId);

    boolean existsByAccountNumber(String accountNumber);

    // Find active accounts
    List<AccountStatementHeader> findByStatusIgnoreCase(String status);

    // Find headers with statement records
    @Query("SELECT DISTINCT h FROM AccountStatementHeader h LEFT JOIN FETCH h.statementRecords WHERE h.id = :id")
    Optional<AccountStatementHeader> findByIdWithStatementRecords(@Param("id") Long id);

    @Query("SELECT DISTINCT h FROM AccountStatementHeader h LEFT JOIN FETCH h.statementRecords WHERE h.caseId = :caseId")
    Optional<AccountStatementHeader> findByCaseIdWithStatementRecords(@Param("caseId") String caseId);

    // Find accounts opened in a specific year
    @Query("SELECT h FROM AccountStatementHeader h WHERE YEAR(h.dateAccountOpened) = :year")
    List<AccountStatementHeader> findByYearAccountOpened(@Param("year") int year);
}