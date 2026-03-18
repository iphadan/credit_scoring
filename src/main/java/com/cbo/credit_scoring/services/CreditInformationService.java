package com.cbo.credit_scoring.services;

import com.cbo.credit_scoring.dtos.CreditInformationRequestDTO;
import com.cbo.credit_scoring.dtos.CreditInformationResponseDTO;
import com.cbo.credit_scoring.dtos.CreditSummaryDTO;
import com.cbo.credit_scoring.models.enums.BankType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CreditInformationService {

    // ============= CRUD Operations =============

    /**
     * Create new credit information records for a case
     * This will create multiple records (one per exposure) in a single transaction
     */
    List<CreditInformationResponseDTO> createCreditInformation(CreditInformationRequestDTO requestDTO);

    /**
     * Update existing credit information records
     * This will replace all exposures for the given case and bank type
     */
    List<CreditInformationResponseDTO> updateCreditInformation(String caseId, BankType bankType, CreditInformationRequestDTO requestDTO);

    /**
     * Get all credit information for a case (both Coop and Other banks)
     */
    List<CreditInformationResponseDTO> getCreditInformationByCaseId(String caseId);
    List<String> getAllCaseIds();
    /**
     * Get credit information for a case by bank type
     */
    CreditInformationResponseDTO getCreditInformationByCaseIdAndBankType(String caseId, BankType bankType);

    /**
     * Get a single credit record by ID
     */
    CreditInformationResponseDTO getCreditRecordById(Long id);

    /**
     * Get all credit records with pagination
     */
    Page<CreditInformationResponseDTO> getAllCreditRecords(Pageable pageable);

    /**
     * Delete all credit information for a case
     */
    void deleteByCaseId(String caseId);

    /**
     * Delete credit information for a case by bank type
     */
    void deleteByCaseIdAndBankType(String caseId, BankType bankType);

    /**
     * Delete a single credit record by ID
     */
    void deleteCreditRecord(Long id);

    // ============= Summary and Statistics =============

    /**
     * Get comprehensive summary for a case
     */
    CreditSummaryDTO getCreditSummary(String caseId);

    /**
     * Calculate totals for a case
     */
    CreditSummaryDTO calculateTotals(String caseId);

    // ============= Validation =============

    /**
     * Validate credit information data
     */
    boolean validateCreditInformation(String caseId);
}