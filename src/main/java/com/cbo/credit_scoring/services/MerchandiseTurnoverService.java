package com.cbo.credit_scoring.services;


import com.cbo.credit_scoring.dtos.MerchandiseTurnoverDTO;
import com.cbo.credit_scoring.dtos.MerchandiseTurnoverDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public interface MerchandiseTurnoverService {
    // Turnover CRUD operations
    MerchandiseTurnoverDTO createTurnover(MerchandiseTurnoverDTO turnoverDTO);
    MerchandiseTurnoverDTO updateTurnover(Long id, MerchandiseTurnoverDTO turnoverDTO);
    MerchandiseTurnoverDTO getTurnoverById(Long id);
    Page<MerchandiseTurnoverDTO> getAllTurnovers(Pageable pageable);
    void deleteTurnover(Long id);

    // Search operations
    List<MerchandiseTurnoverDTO> getTurnoversByHeaderId(Long headerId);
    List<MerchandiseTurnoverDTO> getTurnoversByMonth(YearMonth month);
    List<MerchandiseTurnoverDTO> getTurnoversByDateRange(YearMonth startMonth, YearMonth endMonth);

    // Aggregation operations
    BigDecimal calculateTotalDebitByHeader(Long headerId);
    BigDecimal calculateTotalCreditByHeader(Long headerId);
    BigDecimal calculateNetTurnoverByHeader(Long headerId);
}