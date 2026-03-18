package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.CreditInformation;
import com.cbo.credit_scoring.models.enums.BankType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditInformationRepository extends JpaRepository<CreditInformation, Long> {

    // Add these methods to existing CreditInformationRepository.java

    /**
     * Find all credit information by caseId (already exists as findByCaseIdOrderByBankTypeAscSrNoAsc)
     * But add this simpler version if needed
     */
    List<CreditInformation> findAllByCaseId(String caseId);

    /**
     * Get all unique caseIds from the table
     */
    @Query("SELECT DISTINCT c.caseId FROM CreditInformation c WHERE c.caseId IS NOT NULL")
    List<String> findAllCaseIds();

    /**
     * Check if any records exist for a caseId
     */

    // Find by caseId
    List<CreditInformation> findByCaseId(String caseId);

    // Find by caseId and bank type
    List<CreditInformation> findByCaseIdAndBankType(String caseId, BankType bankType);




    // Find by caseId ordered by bank type and sr no
    List<CreditInformation> findByCaseIdOrderByBankTypeAscSrNoAsc(String caseId);

    // Check existence
    boolean existsByCaseId(String caseId);

    // Delete all by caseId
    void deleteByCaseId(String caseId);

    // ============= Aggregation Queries =============

    @Query("SELECT COALESCE(SUM(c.amountGranted), 0) FROM CreditInformation c WHERE c.caseId = :caseId AND c.bankType = :bankType")
    BigDecimal sumAmountGrantedByCaseIdAndBankType(@Param("caseId") String caseId, @Param("bankType") BankType bankType);

    @Query("SELECT COALESCE(SUM(c.currentBalance), 0) FROM CreditInformation c WHERE c.caseId = :caseId AND c.bankType = :bankType")
    BigDecimal sumCurrentBalanceByCaseIdAndBankType(@Param("caseId") String caseId, @Param("bankType") BankType bankType);

    @Query("SELECT COUNT(c) FROM CreditInformation c WHERE c.caseId = :caseId AND c.bankType = :bankType")
    Long countByCaseIdAndBankType(@Param("caseId") String caseId, @Param("bankType") BankType bankType);

    @Query("SELECT COALESCE(SUM(c.amountGranted), 0) FROM CreditInformation c WHERE c.caseId = :caseId")
    BigDecimal sumTotalAmountGrantedByCaseId(@Param("caseId") String caseId);

    @Query("SELECT COALESCE(SUM(c.currentBalance), 0) FROM CreditInformation c WHERE c.caseId = :caseId")
    BigDecimal sumTotalCurrentBalanceByCaseId(@Param("caseId") String caseId);

    // Group by exposure type
    @Query("SELECT c.exposureType, COUNT(c), COALESCE(SUM(c.amountGranted), 0), COALESCE(SUM(c.currentBalance), 0) " +
            "FROM CreditInformation c WHERE c.caseId = :caseId AND c.bankType = :bankType GROUP BY c.exposureType")
    List<Object[]> groupByExposureType(@Param("caseId") String caseId, @Param("bankType") BankType bankType);

    // Group by status
    @Query("SELECT c.status, COUNT(c), COALESCE(SUM(c.amountGranted), 0), COALESCE(SUM(c.currentBalance), 0) " +
            "FROM CreditInformation c WHERE c.caseId = :caseId AND c.bankType = :bankType GROUP BY c.status")
    List<Object[]> groupByStatus(@Param("caseId") String caseId, @Param("bankType") BankType bankType);
}