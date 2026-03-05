package com.cbo.credit_scoring.services;

import com.cbo.credit_scoring.dtos.PreShipmentTurnoverHeaderDTO;
import com.cbo.credit_scoring.dtos.PreShipmentTurnoverDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface PreShipmentTurnoverHeaderService {

    // Header CRUD operations
    PreShipmentTurnoverHeaderDTO createHeader(PreShipmentTurnoverHeaderDTO headerDTO);
    PreShipmentTurnoverHeaderDTO updateHeader(Long id, PreShipmentTurnoverHeaderDTO headerDTO);
    PreShipmentTurnoverHeaderDTO getHeaderById(Long id);
    PreShipmentTurnoverHeaderDTO getHeaderByCaseId(String caseId);
    Page<PreShipmentTurnoverHeaderDTO> getAllHeaders(Pageable pageable);
    void deleteHeader(Long id);

    // Search operations
    List<PreShipmentTurnoverHeaderDTO> searchByCustomerName(String customerName);
    PreShipmentTurnoverHeaderDTO getHeaderByAccountNumber(String accountNumber);
    List<PreShipmentTurnoverHeaderDTO> getHeadersByTypeOfFacility(String typeOfFacility);
    List<PreShipmentTurnoverHeaderDTO> getHeadersByIndustryType(String industryType);
    List<PreShipmentTurnoverHeaderDTO> getHeadersByDateApprovedRange(LocalDate startDate, LocalDate endDate);
    List<PreShipmentTurnoverHeaderDTO> getHeadersByReportingDate(LocalDate reportingDate);
    List<PreShipmentTurnoverHeaderDTO> searchHeaders(String searchTerm);

    // Turnover record management
    PreShipmentTurnoverHeaderDTO addTurnoverRecord(Long headerId, PreShipmentTurnoverDTO turnoverDTO);
    PreShipmentTurnoverHeaderDTO updateTurnoverRecord(Long headerId, Long turnoverId, PreShipmentTurnoverDTO turnoverDTO);
    PreShipmentTurnoverHeaderDTO removeTurnoverRecord(Long headerId, Long turnoverId);

    // Statistics
    Map<String, Object> getHeaderStatistics(Long headerId);
    BigDecimal calculateTotalDebit(Long headerId);
    BigDecimal calculateTotalCredit(Long headerId);
    BigDecimal calculateNetTurnover(Long headerId);
    List<Map<String, Object>> getMonthlyStatistics(Long headerId);
}