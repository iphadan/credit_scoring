package com.cbo.credit_scoring.services;

import com.cbo.credit_scoring.dtos.OdSwingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface OdSwingService {

    // Swing CRUD operations
    OdSwingDTO createSwing(OdSwingDTO swingDTO);
    OdSwingDTO updateSwing(Long id, OdSwingDTO swingDTO);
    OdSwingDTO getSwingById(Long id);
    Page<OdSwingDTO> getAllSwings(Pageable pageable);
    void deleteSwing(Long id);

    // Find by various criteria
    List<OdSwingDTO> getSwingsByHeaderId(Long headerId);
    List<OdSwingDTO> getSwingsByMonth(YearMonth month);
    List<OdSwingDTO> getSwingsByMonthRange(YearMonth startMonth, YearMonth endMonth);
    OdSwingDTO getSwingByHeaderIdAndMonth(Long headerId, YearMonth month);

    // Find by date ranges
    List<OdSwingDTO> getSwingsByDateHighRange(LocalDate startDate, LocalDate endDate);
    List<OdSwingDTO> getSwingsByDateLowRange(LocalDate startDate, LocalDate endDate);

    // Analytics
    Map<YearMonth, BigDecimal> getMonthlyUtilizationTrend(Long headerId);
    Map<YearMonth, BigDecimal> getMonthlySwingRangeTrend(Long headerId);
    OdSwingDTO getMonthWithHighestUtilization(Long headerId);
    OdSwingDTO getMonthWithLowestUtilization(Long headerId);
    OdSwingDTO getMonthWithWidestSwing(Long headerId);
    List<OdSwingDTO> getHighUtilizationMonths(Long headerId, BigDecimal threshold);

    // Summary
    Map<String, Object> getSwingSummary(Long headerId);
}