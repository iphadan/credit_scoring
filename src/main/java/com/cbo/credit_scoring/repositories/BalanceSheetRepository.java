package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.BalanceSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BalanceSheetRepository extends JpaRepository<BalanceSheet, Long> {

    // Find by financial statement ID ordered by period order
    List<BalanceSheet> findByFinancialStatementIdOrderByPeriodOrder(Long financialStatementId);

    // Find by financial statement ID
    List<BalanceSheet> findByFinancialStatementId(Long financialStatementId);

    // Delete by financial statement ID
    @Modifying
    @Query("DELETE FROM BalanceSheet bs WHERE bs.financialStatement.id = :financialStatementId")
    void deleteByFinancialStatementId(@Param("financialStatementId") Long financialStatementId);

    // Find by case ID (via financial statement)
    @Query("SELECT bs FROM BalanceSheet bs WHERE bs.financialStatement.caseId.id = :caseDatabaseId ORDER BY bs.periodOrder")
    List<BalanceSheet> findByCaseId(@Param("caseDatabaseId") Long caseDatabaseId);

    // Find by caseId string
    @Query("SELECT bs FROM BalanceSheet bs WHERE bs.financialStatement.caseId.caseId = :caseIdString ORDER BY bs.periodOrder")
    List<BalanceSheet> findByCaseIdString(@Param("caseIdString") String caseIdString);

    // Find by period date range
    List<BalanceSheet> findByPeriodDateBetween(LocalDate startDate, LocalDate endDate);

    // Find by financial statement ID and period date
    @Query("SELECT bs FROM BalanceSheet bs WHERE bs.financialStatement.id = :financialStatementId AND bs.periodDate = :periodDate")
    List<BalanceSheet> findByFinancialStatementIdAndPeriodDate(@Param("financialStatementId") Long financialStatementId, @Param("periodDate") LocalDate periodDate);

    // Get distinct period dates for a financial statement
    @Query("SELECT DISTINCT bs.periodDate FROM BalanceSheet bs WHERE bs.financialStatement.id = :financialStatementId ORDER BY bs.periodDate")
    List<LocalDate> findDistinctPeriodDatesByFinancialStatementId(@Param("financialStatementId") Long financialStatementId);

    // Check if period exists for financial statement
    @Query("SELECT CASE WHEN COUNT(bs) > 0 THEN true ELSE false END FROM BalanceSheet bs WHERE bs.financialStatement.id = :financialStatementId AND bs.periodOrder = :periodOrder")
    boolean existsByFinancialStatementIdAndPeriodOrder(@Param("financialStatementId") Long financialStatementId, @Param("periodOrder") Integer periodOrder);

    // Custom aggregation query to get total assets by financial statement
    @Query("SELECT SUM(bs.propertyPlantEquipment + bs.stocks + bs.tradeOtherReceivables + bs.cashOnHandBank) FROM BalanceSheet bs WHERE bs.financialStatement.id = :financialStatementId")
    BigDecimal sumTotalAssetsByFinancialStatementId(@Param("financialStatementId") Long financialStatementId);
}