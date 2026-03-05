package com.cbo.credit_scoring.services;

import com.cbo.credit_scoring.dtos.PreShipmentTurnoverDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface PreShipmentTurnoverService {

    // Turnover CRUD operations
    PreShipmentTurnoverDTO createTurnover(PreShipmentTurnoverDTO turnoverDTO);
    PreShipmentTurnoverDTO updateTurnover(Long id, PreShipmentTurnoverDTO turnoverDTO);
    PreShipmentTurnoverDTO getTurnoverById(Long id);
    Page<PreShipmentTurnoverDTO> getAllTurnovers(Pageable pageable);
    void deleteTurnover(Long id);

    // Find by various criteria
    List<PreShipmentTurnoverDTO> getTurnoversByHeaderId(Long headerId);
    List<PreShipmentTurnoverDTO> getTurnoversByMonth(YearMonth month);
    List<PreShipmentTurnoverDTO> getTurnoversByMonthRange(YearMonth startMonth, YearMonth endMonth);
    PreShipmentTurnoverDTO getTurnoverByHeaderIdAndMonth(Long headerId, YearMonth month);

    // Aggregation operations
    Map<String, Object> getTurnoverSummaryByHeader(Long headerId);
    Map<YearMonth, BigDecimal> getMonthlyDebitTrend(Long headerId);
    Map<YearMonth, BigDecimal> getMonthlyCreditTrend(Long headerId);
}