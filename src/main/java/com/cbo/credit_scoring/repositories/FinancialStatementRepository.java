package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.FinancialStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, Long> {

    // Find by case ID (the database ID, not the caseId string)
    List<FinancialStatement> findByCaseId_Id(Long caseDatabaseId);

    // Find by case ID ordered by version descending
    List<FinancialStatement> findByCaseId_IdOrderByVersionDesc(Long caseDatabaseId);

    // Find top by case ID ordered by version descending
    Optional<FinancialStatement> findTopByCaseId_IdOrderByVersionDesc(Long caseDatabaseId);

    // Find by case ID and version
    Optional<FinancialStatement> findByCaseId_IdAndVersion(Long caseDatabaseId, Integer version);

    // Find by caseId string (the business identifier)
    @Query("SELECT fs FROM FinancialStatement fs WHERE fs.caseId.caseId = :caseIdString ORDER BY fs.version DESC")
    List<FinancialStatement> findByCaseIdString(@Param("caseIdString") String caseIdString);

    // Find by caseId string and version
    @Query("SELECT fs FROM FinancialStatement fs WHERE fs.caseId.caseId = :caseIdString AND fs.version = :version")
    Optional<FinancialStatement> findByCaseIdStringAndVersion(@Param("caseIdString") String caseIdString, @Param("version") Integer version);

    // Find latest by caseId string
    @Query("SELECT fs FROM FinancialStatement fs WHERE fs.caseId.caseId = :caseIdString ORDER BY fs.version DESC LIMIT 1")
    Optional<FinancialStatement> findLatestByCaseIdString(@Param("caseIdString") String caseIdString);

    // Search by company name (case-insensitive partial match)
    List<FinancialStatement> findByCompanyNameContainingIgnoreCase(String companyName);

    // Find by reporting date range
    List<FinancialStatement> findByReportingDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by statement type
    List<FinancialStatement> findByStatementType(String statementType);

    // Custom query to find statements with version greater than specified
    @Query("SELECT fs FROM FinancialStatement fs WHERE fs.caseId.id = :caseDatabaseId AND fs.version > :version")
    List<FinancialStatement> findVersionsGreaterThan(@Param("caseDatabaseId") Long caseDatabaseId, @Param("version") Integer version);

    // Count versions for a case
    @Query("SELECT COUNT(fs) FROM FinancialStatement fs WHERE fs.caseId.id = :caseDatabaseId")
    Long countVersionsByCaseId(@Param("caseDatabaseId") Long caseDatabaseId);

    // Check if version exists for a case
    @Query("SELECT CASE WHEN COUNT(fs) > 0 THEN true ELSE false END FROM FinancialStatement fs WHERE fs.caseId.id = :caseDatabaseId AND fs.version = :version")
    boolean existsByCaseIdAndVersion(@Param("caseDatabaseId") Long caseDatabaseId, @Param("version") Integer version);
}