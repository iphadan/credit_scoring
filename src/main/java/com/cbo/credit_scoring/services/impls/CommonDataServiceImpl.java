package com.cbo.credit_scoring.services.impls;

import com.cbo.credit_scoring.dtos.*;
import com.cbo.credit_scoring.dtos.CaseAggregateDTO;
import com.cbo.credit_scoring.dtos.CaseSummaryDTO;
import com.cbo.credit_scoring.services.*;
import com.cbo.credit_scoring.services.CommonDataService;
import com.cbo.credit_scoring.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonDataServiceImpl implements CommonDataService {

    // Inject all service interfaces
    private final PreShipmentTurnoverHeaderService preShipmentService;
    private final OdTurnoverHeaderService odTurnoverService;
    private final OdSwingHeaderService odSwingService;
    private final MerchandiseTurnoverHeaderService merchandiseService;
    private final AccountStatementHeaderService accountStatementService;
    private final FinancialStatementService financialStatementService;
    private final CreditInformationService creditInformationService;
    private final CollateralService collateralService;

    @Override
    public CaseAggregateDTO getAllDataByCaseId(String caseId) {
        log.info("Fetching all data for caseId: {}", caseId);

        CaseAggregateDTO.CaseAggregateDTOBuilder builder = CaseAggregateDTO.builder()
                .caseId(caseId)
                .generatedAt(LocalDate.now());

        // Fetch data from each module (wrap in try-catch to handle missing data gracefully)
        
        // Pre-shipment Data
        try {
            builder.preShipmentData(preShipmentService.getHeadersByCaseId(caseId));
        } catch (Exception e) {
            log.debug("No pre-shipment data found for case: {}", caseId);
            builder.preShipmentData(Collections.emptyList());
        }

        // OD Turnover Data
        try {
            builder.odTurnoverData(odTurnoverService.getHeadersByCaseId(caseId));
        } catch (Exception e) {
            log.debug("No OD turnover data found for case: {}", caseId);
            builder.odTurnoverData(Collections.emptyList());
        }

        // OD Swing Data
        try {
            builder.odSwingData(odSwingService.getHeadersByCaseId(caseId));
        } catch (Exception e) {
            log.debug("No OD swing data found for case: {}", caseId);
            builder.odSwingData(Collections.emptyList());
        }

        // Merchandise Data
        try {
            builder.merchandiseData(merchandiseService.getHeadersByCaseId(caseId));
        } catch (Exception e) {
            log.debug("No merchandise data found for case: {}", caseId);
            builder.merchandiseData(Collections.emptyList());
        }

        // Account Statement Data
        try {
            builder.accountStatementData(accountStatementService.getHeadersByCaseId(caseId));
        } catch (Exception e) {
            log.debug("No account statement data found for case: {}", caseId);
            builder.accountStatementData(Collections.emptyList());
        }

        // Financial Statement Data
        try {
            builder.financialStatementData(financialStatementService.getFinancialStatementsByCaseId(caseId));
        } catch (Exception e) {
            log.debug("No financial statement data found for case: {}", caseId);
            builder.financialStatementData(Collections.emptyList());
        }

        // Credit Information Data
        try {
            builder.creditInformationData(creditInformationService.getCreditInformationByCaseId(caseId));
        } catch (Exception e) {
            log.debug("No credit information data found for case: {}", caseId);
            builder.creditInformationData(Collections.emptyList());
        }

        // Collateral Data
        try {
            builder.collateralData(collateralService.getCollateralByCaseId(caseId));
        } catch (Exception e) {
            log.debug("No collateral data found for case: {}", caseId);
            builder.collateralData(null);
        }

        // Build summary
        builder.summary(generateSummary(caseId, builder.build()));

        return builder.build();
    }

    @Override
    public CaseSummaryDTO getCaseSummary(String caseId) {
        log.info("Generating summary for caseId: {}", caseId);
        
        CaseAggregateDTO allData = getAllDataByCaseId(caseId);
        return generateSummary(caseId, allData);
    }

    @Override
    public boolean caseExists(String caseId) {
        log.info("Checking if case exists: {}", caseId);
        
        // Check each module
        try {
            if (!preShipmentService.getHeadersByCaseId(caseId).isEmpty()) return true;
        } catch (Exception ignored) {}
        
        try {
            if (!odTurnoverService.getHeadersByCaseId(caseId).isEmpty()) return true;
        } catch (Exception ignored) {}
        
        try {
            if (!odSwingService.getHeadersByCaseId(caseId).isEmpty()) return true;
        } catch (Exception ignored) {}
        
        try {
            if (!merchandiseService.getHeadersByCaseId(caseId).isEmpty()) return true;
        } catch (Exception ignored) {}
        
        try {
            if (!accountStatementService.getHeadersByCaseId(caseId).isEmpty()) return true;
        } catch (Exception ignored) {}
        
        try {
            if (!financialStatementService.getFinancialStatementsByCaseId(caseId).isEmpty()) return true;
        } catch (Exception ignored) {}
        
        try {
            if (!creditInformationService.getCreditInformationByCaseId(caseId).isEmpty()) return true;
        } catch (Exception ignored) {}
        
        try {
            if (collateralService.getCollateralByCaseId(caseId) != null) return true;
        } catch (Exception ignored) {}
        
        return false;
    }

    @Override
    public List<String> getAllCaseIds() {
        log.info("Fetching all unique caseIds from all modules");
        
        Set<String> allCaseIds = new HashSet<>();
        
        // Collect from each module
        allCaseIds.addAll(preShipmentService.getAllCaseIds());
        allCaseIds.addAll(odTurnoverService.getAllCaseIds());
        allCaseIds.addAll(odSwingService.getAllCaseIds());
        allCaseIds.addAll(merchandiseService.getAllCaseIds());
        allCaseIds.addAll(accountStatementService.getAllCaseIds());
        allCaseIds.addAll(financialStatementService.getAllCaseIds());
        allCaseIds.addAll(creditInformationService.getAllCaseIds());
        allCaseIds.addAll(collateralService.getAllCaseIds());
        
        return new ArrayList<>(allCaseIds);
    }

    @Override
    public void deleteAllDataByCaseId(String caseId) {
        log.info("Deleting all data for caseId: {}", caseId);
        
        // Delete from each module (wrap in try-catch to continue even if some fail)
        try {
            preShipmentService.deleteByCaseId(caseId);
        } catch (Exception e) {
            log.warn("Failed to delete pre-shipment data for case: {}", caseId);
        }
        
        try {
            odTurnoverService.deleteByCaseId(caseId);
        } catch (Exception e) {
            log.warn("Failed to delete OD turnover data for case: {}", caseId);
        }
        
        try {
            odSwingService.deleteByCaseId(caseId);
        } catch (Exception e) {
            log.warn("Failed to delete OD swing data for case: {}", caseId);
        }
        
        try {
            merchandiseService.deleteByCaseId(caseId);
        } catch (Exception e) {
            log.warn("Failed to delete merchandise data for case: {}", caseId);
        }
        
        try {
            accountStatementService.deleteByCaseId(caseId);
        } catch (Exception e) {
            log.warn("Failed to delete account statement data for case: {}", caseId);
        }
        
        try {
            financialStatementService.deleteByCaseId(caseId);
        } catch (Exception e) {
            log.warn("Failed to delete financial statement data for case: {}", caseId);
        }
        
        try {
            creditInformationService.deleteByCaseId(caseId);
        } catch (Exception e) {
            log.warn("Failed to delete credit information data for case: {}", caseId);
        }
        
        try {
            collateralService.deleteByCaseId(caseId);
        } catch (Exception e) {
            log.warn("Failed to delete collateral data for case: {}", caseId);
        }
        
        log.info("Successfully deleted all data for caseId: {}", caseId);
    }

    // ============= Helper Methods =============

    private CaseSummaryDTO generateSummary(String caseId, CaseAggregateDTO allData) {
        Map<String, Boolean> modulesAvailable = new HashMap<>();
        Map<String, String> latestReportingDates = new HashMap<>();

        // Pre-shipment
        int preShipmentCount = allData.getPreShipmentData() != null ? allData.getPreShipmentData().size() : 0;
        modulesAvailable.put("preShipment", preShipmentCount > 0);
        if (preShipmentCount > 0 && allData.getPreShipmentData().get(0).getReportingDate() != null) {
            latestReportingDates.put("preShipment", allData.getPreShipmentData().get(0).getReportingDate().toString());
        }

        // OD Turnover
        int odTurnoverCount = allData.getOdTurnoverData() != null ? allData.getOdTurnoverData().size() : 0;
        modulesAvailable.put("odTurnover", odTurnoverCount > 0);
        if (odTurnoverCount > 0 && allData.getOdTurnoverData().get(0).getReportDate() != null) {
            latestReportingDates.put("odTurnover", allData.getOdTurnoverData().get(0).getReportDate().toString());
        }

        // OD Swing
        int odSwingCount = allData.getOdSwingData() != null ? allData.getOdSwingData().size() : 0;
        modulesAvailable.put("odSwing", odSwingCount > 0);
        if (odSwingCount > 0 && allData.getOdSwingData().get(0).getReportDate() != null) {
            latestReportingDates.put("odSwing", allData.getOdSwingData().get(0).getReportDate().toString());
        }

        // Merchandise
        int merchandiseCount = allData.getMerchandiseData() != null ? allData.getMerchandiseData().size() : 0;
        modulesAvailable.put("merchandise", merchandiseCount > 0);
        if (merchandiseCount > 0 && allData.getMerchandiseData().get(0).getReportingDate() != null) {
            latestReportingDates.put("merchandise", allData.getMerchandiseData().get(0).getReportingDate().toString());
        }

        // Account Statement
        int accountStatementCount = allData.getAccountStatementData() != null ? allData.getAccountStatementData().size() : 0;
        modulesAvailable.put("accountStatement", accountStatementCount > 0);
        if (accountStatementCount > 0 && allData.getAccountStatementData().get(0).getReportDate() != null) {
            latestReportingDates.put("accountStatement", allData.getAccountStatementData().get(0).getReportDate().toString());
        }

        // Financial Statement
        int financialStatementCount = allData.getFinancialStatementData() != null ? allData.getFinancialStatementData().size() : 0;
        modulesAvailable.put("financialStatement", financialStatementCount > 0);
        if (financialStatementCount > 0 && allData.getFinancialStatementData().get(0).getReportingDate() != null) {
            latestReportingDates.put("financialStatement", allData.getFinancialStatementData().get(0).getReportingDate().toString());
        }

        // Credit Information
        int creditInformationCount = allData.getCreditInformationData() != null ? allData.getCreditInformationData().size() : 0;
        modulesAvailable.put("creditInformation", creditInformationCount > 0);
        if (creditInformationCount > 0 && allData.getCreditInformationData().get(0).getReportingDate() != null) {
            latestReportingDates.put("creditInformation", allData.getCreditInformationData().get(0).getReportingDate().toString());
        }

        // Collateral
        boolean hasCollateral = allData.getCollateralData() != null && 
                allData.getCollateralData().getCollaterals() != null && 
                !allData.getCollateralData().getCollaterals().isEmpty();
        modulesAvailable.put("collateral", hasCollateral);
        if (hasCollateral && allData.getCollateralData().getReportingDate() != null) {
            latestReportingDates.put("collateral", allData.getCollateralData().getReportingDate().toString());
        }

        // Calculate financial totals
        BigDecimal totalPreShipmentAmount = calculatePreShipmentTotal(allData);
        BigDecimal totalOdTurnoverAmount = calculateOdTurnoverTotal(allData);
        BigDecimal totalCollateralValue = hasCollateral ? allData.getCollateralData().getTotalValue() : BigDecimal.ZERO;
        BigDecimal totalCollateralNetValue = hasCollateral ? allData.getCollateralData().getTotalNetValue() : BigDecimal.ZERO;

        return CaseSummaryDTO.builder()
                .preShipmentCount(preShipmentCount)
                .odTurnoverCount(odTurnoverCount)
                .odSwingCount(odSwingCount)
                .merchandiseCount(merchandiseCount)
                .accountStatementCount(accountStatementCount)
                .financialStatementCount(financialStatementCount)
                .creditInformationCount(creditInformationCount)
                .collateralCount(hasCollateral ? allData.getCollateralData().getTotalRecords() : 0)
                .totalPreShipmentAmount(totalPreShipmentAmount)
                .totalOdTurnoverAmount(totalOdTurnoverAmount)
                .totalCollateralValue(totalCollateralValue)
                .totalCollateralNetValue(totalCollateralNetValue)
                .modulesAvailable(modulesAvailable)
                .latestReportingDates(latestReportingDates)
                .build();
    }

    private BigDecimal calculatePreShipmentTotal(CaseAggregateDTO allData) {
        if (allData.getPreShipmentData() == null || allData.getPreShipmentData().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return allData.getPreShipmentData().stream()
                .flatMap(header -> header.getTurnoverRecords().stream())
                .map(record -> {
                    BigDecimal debit = record.getDebitDisbursements() != null ? 
                            record.getDebitDisbursements() : BigDecimal.ZERO;
                    BigDecimal credit = record.getCreditPrincipalRepayments() != null ? 
                            record.getCreditPrincipalRepayments() : BigDecimal.ZERO;
                    return debit.add(credit);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateOdTurnoverTotal(CaseAggregateDTO allData) {
        if (allData.getOdTurnoverData() == null || allData.getOdTurnoverData().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return allData.getOdTurnoverData().stream()
                .flatMap(header -> header.getTurnoverRecords().stream())
                .map(record -> {
                    BigDecimal credit = record.getTotalTurnoverCredit() != null ? 
                            record.getTotalTurnoverCredit() : BigDecimal.ZERO;
                    BigDecimal debit = record.getTotalTurnoverDebit() != null ? 
                            record.getTotalTurnoverDebit() : BigDecimal.ZERO;
                    return credit.add(debit);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}