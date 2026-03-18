package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.Collateral;
import com.cbo.credit_scoring.models.enums.CollateralType;
import com.cbo.credit_scoring.models.enums.ValuationMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CollateralRepository extends JpaRepository<Collateral, Long> {

    // Find by caseId
    List<Collateral> findByCaseIdOrderBySrNoAsc(String caseId);

    // Check existence
    boolean existsByCaseId(String caseId);

    // Delete all by caseId
    void deleteByCaseId(String caseId);

    // ============= Aggregation Queries =============
// Add these methods to existing CollateralRepository.java

    /**
     * Find all collateral by caseId (already exists as findByCaseIdOrderBySrNoAsc)
     * But add this simpler version if needed
     */
    List<Collateral> findAllByCaseId(String caseId);

    /**
     * Get all unique caseIds from the table
     */
    @Query("SELECT DISTINCT c.caseId FROM Collateral c WHERE c.caseId IS NOT NULL")
    List<String> findAllCaseIds();

    /**
     * Check if any records exist for a caseId
     */
    @Query("SELECT COALESCE(SUM(c.value), 0) FROM Collateral c WHERE c.caseId = :caseId")
    BigDecimal sumValueByCaseId(@Param("caseId") String caseId);

    @Query("SELECT COUNT(c) FROM Collateral c WHERE c.caseId = :caseId")
    Long countByCaseId(@Param("caseId") String caseId);

    // Group by collateral type
    @Query("SELECT c.collateralType, COUNT(c), COALESCE(SUM(c.value), 0) " +
            "FROM Collateral c WHERE c.caseId = :caseId GROUP BY c.collateralType")
    List<Object[]> groupByCollateralType(@Param("caseId") String caseId);

    // Group by valuation method
    @Query("SELECT c.valuationMethod, COUNT(c), COALESCE(SUM(c.value), 0) " +
            "FROM Collateral c WHERE c.caseId = :caseId GROUP BY c.valuationMethod")
    List<Object[]> groupByValuationMethod(@Param("caseId") String caseId);
}