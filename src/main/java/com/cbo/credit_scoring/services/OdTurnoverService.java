package com.cbo.credit_scoring.services;

import com.cbo.credit_scoring.dtos.OdTurnoverDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface OdTurnoverService {

    // Turnover CRUD operations
    OdTurnoverDTO createTurnover(OdTurnoverDTO turnoverDTO);
    OdTurnoverDTO updateTurnover(Long id, OdTurnoverDTO turnoverDTO);
    OdTurnoverDTO getTurnoverById(Long id);
    Page<OdTurnoverDTO> getAllTurnovers(Pageable pageable);
    void deleteTurnover(Long id);

    // Find by various criteria
    List<OdTurnoverDTO> getTurnoversByHeaderId(Long headerId);
    List<OdTurnoverDTO> getTurnoversByMonth(YearMonth month);
    List<OdTurnoverDTO> getTurnoversByMonthRange(YearMonth startMonth, YearMonth endMonth);
    OdTurnoverDTO getTurnoverByHeaderIdAndMonth(Long headerId, YearMonth month);

    // Analytics
    Map<YearMonth, BigDecimal> getMonthlyCreditTrend(Long headerId);
    Map<YearMonth, BigDecimal> getMonthlyDebitTrend(Long headerId);
    Map<YearMonth, BigDecimal> getMonthlyUtilizationTrend(Long headerId);
    List<OdTurnoverDTO> getHighUtilizationMonths(Long headerId, BigDecimal threshold);

    // Summary
    Map<String, Object> getTurnoverSummary(Long headerId);
}