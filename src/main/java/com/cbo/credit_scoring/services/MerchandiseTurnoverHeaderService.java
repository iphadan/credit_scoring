package com.cbo.credit_scoring.services;


import com.cbo.credit_scoring.dtos.MerchandiseTurnoverHeaderDTO;
import com.cbo.credit_scoring.dtos.MerchandiseTurnoverDTO;
import com.cbo.credit_scoring.models.MerchandiseTurnoverHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface MerchandiseTurnoverHeaderService {
    // Header CRUD operations
    MerchandiseTurnoverHeaderDTO createHeader(MerchandiseTurnoverHeaderDTO headerDTO);
    MerchandiseTurnoverHeaderDTO updateHeader(Long id, MerchandiseTurnoverHeaderDTO headerDTO);
    MerchandiseTurnoverHeaderDTO getHeaderById(Long id);
    Page<MerchandiseTurnoverHeaderDTO> getAllHeaders(Pageable pageable);
    void deleteHeader(Long id);

    // Search operations
    List<MerchandiseTurnoverHeaderDTO> searchByCustomerName(String customerName);
    List<MerchandiseTurnoverHeaderDTO> searchByAccountNumber(String accountNumber);
    List<MerchandiseTurnoverHeaderDTO> searchByDateApprovedBetween(LocalDate startDate, LocalDate endDate);
    List<MerchandiseTurnoverHeaderDTO> searchByReportingDate(LocalDate reportingDate);

    // Turnover record management within header
    MerchandiseTurnoverHeaderDTO addTurnoverRecord(Long headerId, MerchandiseTurnoverDTO turnoverDTO);
    MerchandiseTurnoverHeaderDTO removeTurnoverRecord(Long headerId, Long turnoverId);
    MerchandiseTurnoverHeaderDTO updateTurnoverRecord(Long headerId, Long turnoverId, MerchandiseTurnoverDTO turnoverDTO);
}