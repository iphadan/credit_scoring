package com.cbo.credit_scoring.services;

import com.cbo.credit_scoring.dtos.AccountStatementHeaderDTO;
import com.cbo.credit_scoring.dtos.AccountStatementDTO;
import com.cbo.credit_scoring.dtos.AccountStatementSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface AccountStatementHeaderService {

    // Header CRUD operations
    AccountStatementHeaderDTO createHeader(AccountStatementHeaderDTO headerDTO);
    AccountStatementHeaderDTO updateHeader(Long id, AccountStatementHeaderDTO headerDTO);
    AccountStatementHeaderDTO getHeaderById(Long id);
    AccountStatementHeaderDTO getHeaderByCaseId(String caseId);
    Page<AccountStatementHeaderDTO> getAllHeaders(Pageable pageable);
    void deleteHeader(Long id);

    // Search operations
    List<AccountStatementHeaderDTO> searchByAccountHolder(String accountHolder);
    AccountStatementHeaderDTO getHeaderByAccountNumber(String accountNumber);
    List<AccountStatementHeaderDTO> getHeadersByIndustry(String industry);
    List<AccountStatementHeaderDTO> getHeadersByFacilityType(String facilityType);
    List<AccountStatementHeaderDTO> getHeadersByStatus(String status);
    List<AccountStatementHeaderDTO> getHeadersByApprovedDateRange(LocalDate startDate, LocalDate endDate);
    List<AccountStatementHeaderDTO> getHeadersByDateAccountOpenedRange(LocalDate startDate, LocalDate endDate);
    List<AccountStatementHeaderDTO> getHeadersByReportDate(LocalDate reportDate);
    List<AccountStatementHeaderDTO> searchHeaders(String searchTerm);

    // Statement record management
    AccountStatementHeaderDTO addStatementRecord(Long headerId, AccountStatementDTO statementDTO);
    AccountStatementHeaderDTO updateStatementRecord(Long headerId, Long statementId, AccountStatementDTO statementDTO);
    AccountStatementHeaderDTO removeStatementRecord(Long headerId, Long statementId);

    // Statistics and summaries
    AccountStatementSummaryDTO getHeaderSummary(Long headerId);
    Map<String, Object> getHeaderStatistics(Long headerId);
    List<AccountStatementSummaryDTO> getAllHeadersSummary();

    // Analytics
    BigDecimal calculateTotalCredit(Long headerId);
    BigDecimal calculateTotalDebit(Long headerId);
    BigDecimal calculateNetTurnover(Long headerId);
    BigDecimal calculateAverageUtilization(Long headerId);
    BigDecimal calculateMaxUtilization(Long headerId);
    BigDecimal calculateMinUtilization(Long headerId);
    Map<YearMonth, BigDecimal> getUtilizationTrend(Long headerId);
    Map<YearMonth, BigDecimal> getCreditTrend(Long headerId);
    Map<YearMonth, BigDecimal> getDebitTrend(Long headerId);
    List<AccountStatementDTO> getMonthsExceedingThreshold(Long headerId, BigDecimal threshold);
}