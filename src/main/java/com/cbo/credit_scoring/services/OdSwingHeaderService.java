package com.cbo.credit_scoring.services;

import com.cbo.credit_scoring.dtos.OdSwingHeaderDTO;
import com.cbo.credit_scoring.dtos.OdSwingDTO;
import com.cbo.credit_scoring.dtos.OdSwingSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface OdSwingHeaderService {
    List<OdSwingHeaderDTO> getHeadersByCaseId(String caseId);
    void deleteByCaseId(String caseId);
    List<String> getAllCaseIds();
    // Header CRUD operations
    OdSwingHeaderDTO createHeader(OdSwingHeaderDTO headerDTO);
    OdSwingHeaderDTO updateHeader(Long id, OdSwingHeaderDTO headerDTO);
    OdSwingHeaderDTO getHeaderById(Long id);
    OdSwingHeaderDTO getHeaderByCaseId(String caseId);
    Page<OdSwingHeaderDTO> getAllHeaders(Pageable pageable);
    void deleteHeader(Long id);

    // Search operations
    List<OdSwingHeaderDTO> searchByAccountHolder(String accountHolder);
    OdSwingHeaderDTO getHeaderByAccountNumber(String accountNumber);
    List<OdSwingHeaderDTO> getHeadersByIndustry(String industry);
    List<OdSwingHeaderDTO> getHeadersByFacilityType(String facilityType);
    List<OdSwingHeaderDTO> getHeadersByStatus(String status);
    List<OdSwingHeaderDTO> getHeadersByApprovedDateRange(LocalDate startDate, LocalDate endDate);
    List<OdSwingHeaderDTO> getHeadersByReportDate(LocalDate reportDate);
    List<OdSwingHeaderDTO> searchHeaders(String searchTerm);

    // Swing record management
    OdSwingHeaderDTO addSwingRecord(Long headerId, OdSwingDTO swingDTO);
    OdSwingHeaderDTO updateSwingRecord(Long headerId, Long swingId, OdSwingDTO swingDTO);
    OdSwingHeaderDTO removeSwingRecord(Long headerId, Long swingId);

    // Statistics and summaries
    OdSwingSummaryDTO getHeaderSummary(Long headerId);
    Map<String, Object> getHeaderStatistics(Long headerId);
    List<OdSwingSummaryDTO> getAllHeadersSummary();

    // Analytics
    BigDecimal calculateAverageUtilization(Long headerId);
    BigDecimal calculateMaxUtilization(Long headerId);
    BigDecimal calculateMinUtilization(Long headerId);
    BigDecimal calculateAverageSwingRange(Long headerId);
    Map<YearMonth, BigDecimal> getUtilizationTrend(Long headerId);
    Map<YearMonth, BigDecimal> getSwingRangeTrend(Long headerId);
    List<OdSwingDTO> getMonthsExceedingThreshold(Long headerId, BigDecimal threshold);
}