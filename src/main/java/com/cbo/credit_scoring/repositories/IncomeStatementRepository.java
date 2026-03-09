package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.IncomeStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeStatementRepository extends JpaRepository<IncomeStatement, Long> {

    // Find by financial statement ID ordered by period order
    List<IncomeStatement> findByFinancialStatementIdOrderByPeriodOrder(Long financialStatementId);

    // Find by financial statement ID
    List<IncomeStatement> findByFinancialStatementId(Long financialStatementId);

    // Delete by financial statement ID
    @Modifying
    @Query("DELETE FROM IncomeStatement is WHERE is.financialStatement.id = :financialStatementId")
    void deleteByFinancialStatementId(@Param("financialStatementId") Long financialStatementId);

    // Find by case ID (via financial statement)
    @Query("SELECT is FROM IncomeStatement is WHERE is.financialStatement.caseId.id = :caseDatabaseId ORDER BY is.periodOrder")
    List<IncomeStatement> findByCaseId(@Param("caseDatabaseId") Long caseDatabaseId);

    // Find by caseId string
    @Query("SELECT is FROM IncomeStatement is WHERE is.financialStatement.caseId.caseId = :caseIdString ORDER BY is.periodOrder")
    List<IncomeStatement> findByCaseIdString(@Param("caseIdString") String caseIdString);

    // Find by period date range
    List<IncomeStatement> findByPeriodDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by financial statement ID and period date
    @Query("SELECT is FROM IncomeStatement is WHERE is.financialStatement.id = :financialStatementId AND is.periodDate = :periodDate")
    List<IncomeStatement> findByFinancialStatementIdAndPeriodDate(@Param("financialStatementId") Long financialStatementId, @Param("periodDate") LocalDate periodDate);

    // Get distinct period dates for a financial statement
    @Query("SELECT DISTINCT is.periodDate FROM IncomeStatement is WHERE is.financialStatement.id = :financialStatementId ORDER BY is.periodDate")
    List<LocalDate> findDistinctPeriodDatesByFinancialStatementId(@Param("financialStatementId") Long financialStatementId);

    // Check if period exists for financial statement
    @Query("SELECT CASE WHEN COUNT(is) > 0 THEN true ELSE false END FROM IncomeStatement is WHERE is.financialStatement.id = :financialStatementId AND is.periodOrder = :periodOrder")
    boolean existsByFinancialStatementIdAndPeriodOrder(@Param("financialStatementId") Long financialStatementId, @Param("periodOrder") Integer periodOrder);

    // Custom aggregation query to get total revenue by financial statement
    @Query("SELECT SUM(is.sales) FROM IncomeStatement is WHERE is.financialStatement.id = :financialStatementId")
    BigDecimal sumTotalRevenueByFinancialStatementId(@Param("financialStatementId") Long financialStatementId);

//    // Custom aggregation query to get total net income by financial statement
//    @Query("SELECT SUM(is.netIncome) FROM IncomeStatement is WHERE is.financialStatement.id = :financialStatementId")
//    BigDecimal sumTotalNetIncomeByFinancialStatementId(@Param("financialStatementId") Long financialStatementId);
}