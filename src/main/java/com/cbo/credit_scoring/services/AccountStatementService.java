package com.cbo.credit_scoring.services;

import com.cbo.credit_scoring.dtos.AccountStatementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface AccountStatementService {

    // Statement CRUD operations
    AccountStatementDTO createStatement(AccountStatementDTO statementDTO);
    AccountStatementDTO updateStatement(Long id, AccountStatementDTO statementDTO);
    AccountStatementDTO getStatementById(Long id);
    Page<AccountStatementDTO> getAllStatements(Pageable pageable);
    void deleteStatement(Long id);

    // Find by various criteria
    List<AccountStatementDTO> getStatementsByHeaderId(Long headerId);
    List<AccountStatementDTO> getStatementsByHeaderIdOrdered(Long headerId, boolean ascending);
    List<AccountStatementDTO> getStatementsByMonth(YearMonth month);
    List<AccountStatementDTO> getStatementsByMonthRange(YearMonth startMonth, YearMonth endMonth);
    AccountStatementDTO getStatementByHeaderIdAndMonth(Long headerId, YearMonth month);

    // Analytics
    Map<YearMonth, BigDecimal> getMonthlyCreditTrend(Long headerId);
    Map<YearMonth, BigDecimal> getMonthlyDebitTrend(Long headerId);
    Map<YearMonth, BigDecimal> getMonthlyUtilizationTrend(Long headerId);
    Map<YearMonth, BigDecimal> getMonthlyNetTurnoverTrend(Long headerId);

    // Special queries
    AccountStatementDTO getMonthWithHighestUtilization(Long headerId);
    AccountStatementDTO getMonthWithLowestUtilization(Long headerId);
    AccountStatementDTO getMonthWithHighestCredit(Long headerId);
    AccountStatementDTO getMonthWithHighestDebit(Long headerId);
    AccountStatementDTO getLatestStatement(Long headerId);
    AccountStatementDTO getOldestStatement(Long headerId);

    List<AccountStatementDTO> getHighUtilizationMonths(Long headerId, BigDecimal threshold);
    List<AccountStatementDTO> getLowUtilizationMonths(Long headerId, BigDecimal threshold);

    // Summary
    Map<String, Object> getStatementSummary(Long headerId);
    Map<String, Object> getComparativeAnalysis(Long headerId1, Long headerId2);
}