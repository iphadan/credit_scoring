package com.cbo.credit_scoring.services;

import com.cbo.credit_scoring.dtos.FinancialStatementRequestDTO;
import com.cbo.credit_scoring.dtos.FinancialStatementResponseDTO;
import com.cbo.credit_scoring.dtos.FinancialStatementSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FinancialStatementService {

    void deleteByCaseId(String caseId);
    List<String> getAllCaseIds();
    // ============= Basic CRUD Operations =============

    /**
     * Create a new financial statement for a case
     */
    FinancialStatementResponseDTO createFinancialStatement(FinancialStatementRequestDTO requestDTO);

    /**
     * Update an existing financial statement
     */
    FinancialStatementResponseDTO updateFinancialStatement(Long id, FinancialStatementRequestDTO requestDTO);

    /**
     * Get financial statement by ID
     */
    FinancialStatementResponseDTO getFinancialStatementById(Long id);

    /**
     * Get all financial statements with pagination
     */
    Page<FinancialStatementResponseDTO> getAllFinancialStatements(Pageable pageable);

    /**
     * Delete a financial statement
     */
    void deleteFinancialStatement(Long id);

    // ============= Case-based Operations =============

    /**
     * Get all financial statements for a specific case
     */
    List<FinancialStatementResponseDTO> getFinancialStatementsByCaseId(String caseId);

    /**
     * Get the latest version of financial statement for a case
     */
    FinancialStatementResponseDTO getLatestFinancialStatementByCaseId(String caseId);

    /**
     * Get a specific version of financial statement for a case
     */
    FinancialStatementResponseDTO getFinancialStatementByCaseIdAndVersion(String caseId, Integer version);

    // ============= Search Operations =============

    /**
     * Search financial statements by company name
     */
    List<FinancialStatementResponseDTO> searchByCompanyName(String companyName);

    /**
     * Search financial statements by date range
     */
    List<FinancialStatementResponseDTO> getFinancialStatementsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Search financial statements by statement type
     */
    List<FinancialStatementResponseDTO> getFinancialStatementsByType(String statementType);

    // ============= Analysis and Summary =============

    /**
     * Get comprehensive summary of a financial statement
     */
    FinancialStatementSummaryDTO getFinancialStatementSummary(Long id);

    /**
     * Compare two financial statements (different versions or different cases)
     */
    Map<String, Object> compareFinancialStatements(Long id1, Long id2);

    /**
     * Generate ratio analysis report
     */
    Map<String, Object> generateRatioAnalysis(Long id);

    /**
     * Get trend analysis for key metrics
     */
    Map<String, List<BigDecimal>> getTrendAnalysis(Long id, List<String> metrics);

    /**
     * Export financial statement data (for CSV/Excel generation)
     */
    Map<String, Object> exportFinancialStatementData(Long id);

    // ============= Validation Operations =============

    /**
     * Validate financial statement data (check for discrepancies)
     */
    Map<String, Object> validateFinancialStatement(Long id);

    /**
     * Check if balance sheet balances (Assets = Liabilities + Equity)
     */
    boolean isBalanceSheetBalanced(Long id);
}