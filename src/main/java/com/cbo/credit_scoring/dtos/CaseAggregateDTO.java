package com.cbo.credit_scoring.dtos;

import com.cbo.credit_scoring.dtos.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseAggregateDTO {

    private String caseId;
    private LocalDate generatedAt;

    // Pre-shipment Turnover
    private List<PreShipmentTurnoverHeaderDTO> preShipmentData;

    // OD Turnover
    private List<OdTurnoverHeaderDTO> odTurnoverData;

    // OD Swing
    private List<OdSwingHeaderDTO> odSwingData;

    // Merchandise Turnover
    private List<MerchandiseTurnoverHeaderDTO> merchandiseData;

    // Account Statement
    private List<AccountStatementHeaderDTO> accountStatementData;

    // Financial Statement
    private List<FinancialStatementResponseDTO> financialStatementData;

    // Credit Information
    private List<CreditInformationResponseDTO> creditInformationData;

    // Collateral
    private CollateralResponseDTO collateralData;

    // Summary statistics
    private CaseSummaryDTO summary;
}