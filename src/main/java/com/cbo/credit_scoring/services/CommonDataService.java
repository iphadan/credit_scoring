package com.cbo.credit_scoring.services;

import com.cbo.credit_scoring.dtos.CaseAggregateDTO;
import com.cbo.credit_scoring.dtos.CaseSummaryDTO;

import java.util.List;

public interface CommonDataService {

    /**
     * Get all data for a specific caseId from all modules
     */
    CaseAggregateDTO getAllDataByCaseId(String caseId);

    /**
     * Get summary statistics for a case
     */
    CaseSummaryDTO getCaseSummary(String caseId);

    /**
     * Check if a case exists in any module
     */
    boolean caseExists(String caseId);

    /**
     * Get list of all unique caseIds across all modules
     */
    List<String> getAllCaseIds();

    /**
     * Delete all data for a caseId from all modules
     */
    void deleteAllDataByCaseId(String caseId);
}