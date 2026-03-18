package com.cbo.credit_scoring.services;

import com.cbo.credit_scoring.dtos.OdTurnoverHeaderDTO;
import com.cbo.credit_scoring.dtos.OdTurnoverDTO;
import com.cbo.credit_scoring.dtos.OdTurnoverSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface OdTurnoverHeaderService {

    // Header CRUD operations
    OdTurnoverHeaderDTO createHeader(OdTurnoverHeaderDTO headerDTO);
    OdTurnoverHeaderDTO updateHeader(Long id, OdTurnoverHeaderDTO headerDTO);
    OdTurnoverHeaderDTO getHeaderById(Long id);
    OdTurnoverHeaderDTO getHeaderByCaseId(String caseId);
    Page<OdTurnoverHeaderDTO> getAllHeaders(Pageable pageable);
    List<OdTurnoverHeaderDTO> getHeadersByCaseId(String caseId);
    void deleteByCaseId(String caseId);
    List<String> getAllCaseIds();

    void deleteHeader(Long id);

    // Search operations
    List<OdTurnoverHeaderDTO> searchByAccountHolder(String accountHolder);
    OdTurnoverHeaderDTO getHeaderByAccountNumber(String accountNumber);
    List<OdTurnoverHeaderDTO> getHeadersByIndustry(String industry);
    List<OdTurnoverHeaderDTO> getHeadersByFacilityType(String facilityType);
    List<OdTurnoverHeaderDTO> getHeadersByStatus(String status);
    List<OdTurnoverHeaderDTO> getHeadersByApprovedDateRange(LocalDate startDate, LocalDate endDate);
    List<OdTurnoverHeaderDTO> getHeadersByReportDate(LocalDate reportDate);
    List<OdTurnoverHeaderDTO> searchHeaders(String searchTerm);

    // Turnover record management
    OdTurnoverHeaderDTO addTurnoverRecord(Long headerId, OdTurnoverDTO turnoverDTO);
    OdTurnoverHeaderDTO updateTurnoverRecord(Long headerId, Long turnoverId, OdTurnoverDTO turnoverDTO);
    OdTurnoverHeaderDTO removeTurnoverRecord(Long headerId, Long turnoverId);

    // Statistics and summaries
    OdTurnoverSummaryDTO getHeaderSummary(Long headerId);
    Map<String, Object> getHeaderStatistics(Long headerId);
    List<OdTurnoverSummaryDTO> getAllHeadersSummary();

    // Analytics
    BigDecimal calculateTotalCredit(Long headerId);
    BigDecimal calculateTotalDebit(Long headerId);
    BigDecimal calculateNetTurnover(Long headerId);
    BigDecimal calculateAverageUtilization(Long headerId);
    Map<YearMonth, BigDecimal> getUtilizationTrend(Long headerId);
}