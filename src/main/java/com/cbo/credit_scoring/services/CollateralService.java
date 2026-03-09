package com.cbo.credit_scoring.services;

import com.cbo.credit_scoring.dtos.CollateralRequestDTO;
import com.cbo.credit_scoring.dtos.CollateralResponseDTO;
import com.cbo.credit_scoring.dtos.CollateralSummaryDTO;
import com.cbo.credit_scoring.models.enums.CollateralType;
import com.cbo.credit_scoring.models.enums.ValuationMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CollateralService {

    // ============= CRUD Operations =============

    /**
     * Create new collateral records for a case
     */
    CollateralResponseDTO createCollateral(CollateralRequestDTO requestDTO);

    /**
     * Update collateral records for a case (replaces all)
     */
    CollateralResponseDTO updateCollateral(String caseId, CollateralRequestDTO requestDTO);

    /**
     * Get all collateral for a case
     */
    CollateralResponseDTO getCollateralByCaseId(String caseId);

    /**
     * Get a single collateral record by ID
     */
    CollateralResponseDTO.CollateralRecord getCollateralRecordById(Long id);

    /**
     * Get all collateral records with pagination
     */
    Page<CollateralResponseDTO.CollateralRecord> getAllCollateralRecords(Pageable pageable);

    /**
     * Delete all collateral for a case
     */
    void deleteByCaseId(String caseId);

    /**
     * Delete a single collateral record by ID
     */
    void deleteCollateralRecord(Long id);

    // ============= Summary and Statistics =============

    /**
     * Get comprehensive summary for a case
     */
    CollateralSummaryDTO getCollateralSummary(String caseId);

    /**
     * Calculate totals for a case
     */
    CollateralSummaryDTO calculateTotals(String caseId);

    // ============= Business Rule Calculations =============

    /**
     * Calculate age from year of manufacturing
     */
    Integer calculateAge(Integer yearOfManufacturing);

    /**
     * Calculate mandatory method (0 or 1) based on Excel rules
     */
    Boolean calculateMandatoryMethod(CollateralType type, Integer age, ValuationMethod method);

    /**
     * Calculate discount rate based on complex Excel rules
     */
    BigDecimal calculateDiscountRate(CollateralType type, Integer age, ValuationMethod method);

    /**
     * Calculate net value (value * discount rate)
     */
    BigDecimal calculateNetValue(BigDecimal value, BigDecimal discountRate);
}