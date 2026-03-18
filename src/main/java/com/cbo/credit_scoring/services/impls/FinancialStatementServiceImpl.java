package com.cbo.credit_scoring.services.impls;

import com.cbo.credit_scoring.dtos.FinancialStatementRequestDTO;
import com.cbo.credit_scoring.dtos.FinancialStatementResponseDTO;
import com.cbo.credit_scoring.dtos.FinancialStatementSummaryDTO;
import com.cbo.credit_scoring.dtos.FinancialRatioDTO;
import com.cbo.credit_scoring.models.Case;
import com.cbo.credit_scoring.models.*;
import com.cbo.credit_scoring.repositories.CaseRepository;
import com.cbo.credit_scoring.repositories.*;
import com.cbo.credit_scoring.services.FinancialStatementService;
import com.cbo.credit_scoring.services.impls.FinancialRatioCalculator;
import com.cbo.credit_scoring.exceptions.ResourceNotFoundException;
import com.cbo.credit_scoring.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FinancialStatementServiceImpl implements FinancialStatementService {

    private final CaseRepository caseRepository;
    private final FinancialStatementRepository financialStatementRepository;
    private final BalanceSheetRepository balanceSheetRepository;
    private final IncomeStatementRepository incomeStatementRepository;
    private final FinancialRatioCalculator ratioCalculator;

    private static final BigDecimal THIRTY_FIVE_PERCENT = new BigDecimal("35");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
// Add these methods to existing FinancialStatementServiceImpl.java

    @Override
    public List<FinancialStatementResponseDTO> getFinancialStatementsByCaseId(String caseId) {
        log.info("Fetching all financial statements for caseId: {}", caseId);

        List<FinancialStatement> statements = financialStatementRepository.findAllByCaseId(caseId);

        List<FinancialStatementResponseDTO> responses = new ArrayList<>();
        for (FinancialStatement statement : statements) {
            List<BalanceSheet> balanceSheets = balanceSheetRepository
                    .findByFinancialStatementIdOrderByPeriodOrder(statement.getId());
            List<IncomeStatement> incomeStatements = incomeStatementRepository
                    .findByFinancialStatementIdOrderByPeriodOrder(statement.getId());

            responses.add(buildResponse(statement, balanceSheets, incomeStatements));
        }

        return responses;
    }

    @Override
    public void deleteByCaseId(String caseId) {
        log.info("Deleting all financial statement data for caseId: {}", caseId);

        List<FinancialStatement> statements = financialStatementRepository.findAllByCaseId(caseId);
        if (!statements.isEmpty()) {
            financialStatementRepository.deleteAll(statements);
            log.info("Deleted {} financial statement records for caseId: {}", statements.size(), caseId);
        } else {
            log.debug("No financial statement data found for caseId: {}", caseId);
        }
    }

    @Override
    public List<String> getAllCaseIds() {
        log.info("Fetching all unique caseIds from financial statement module");

        return financialStatementRepository.findAllCaseIds();
    }
    @Override
    public FinancialStatementResponseDTO createFinancialStatement(FinancialStatementRequestDTO requestDTO) {
        log.info("Creating financial statement for case: {}", requestDTO.getCaseId());

        // Validate request
        validateFinancialStatementRequest(requestDTO);

        // Find the case
        Case case_ = caseRepository.findByCaseId(requestDTO.getCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + requestDTO.getCaseId()));

        // Get next version number
        Integer nextVersion = getNextVersionNumber(case_.getId());

        // Create main financial statement
        FinancialStatement statement = FinancialStatement.builder()
                .caseId(case_)
                .companyName(requestDTO.getCompanyName() != null ?
                        requestDTO.getCompanyName() : case_.getCaseId())
                .statementType(requestDTO.getStatementType() != null ?
                        requestDTO.getStatementType() : "COMPREHENSIVE")
                .reportingDate(requestDTO.getReportingDate() != null ?
                        requestDTO.getReportingDate() : LocalDate.now())
                .version(nextVersion)

                .build();

        FinancialStatement savedStatement = financialStatementRepository.save(statement);

        // Save periods data
        savePeriodsData(savedStatement, requestDTO.getPeriods());

        log.info("Financial statement created successfully with ID: {}, Version: {}",
                savedStatement.getId(), savedStatement.getVersion());

        return getFinancialStatementById(savedStatement.getId());
    }

    @Override
    public FinancialStatementResponseDTO updateFinancialStatement(Long id, FinancialStatementRequestDTO requestDTO) {
        log.info("Updating financial statement with ID: {}", id);

        FinancialStatement existingStatement = financialStatementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial statement not found with ID: " + id));

        // Validate request
        validateFinancialStatementRequest(requestDTO);

        // Update basic fields
        if (requestDTO.getCompanyName() != null) {
            existingStatement.setCompanyName(requestDTO.getCompanyName());
        }
        if (requestDTO.getStatementType() != null) {
            existingStatement.setStatementType(requestDTO.getStatementType());
        }
        if (requestDTO.getReportingDate() != null) {
            existingStatement.setReportingDate(requestDTO.getReportingDate());
        }

        // Delete existing periods data
        balanceSheetRepository.deleteByFinancialStatementId(existingStatement.getId());
        incomeStatementRepository.deleteByFinancialStatementId(existingStatement.getId());

        // Save new periods data
        savePeriodsData(existingStatement, requestDTO.getPeriods());

        FinancialStatement updatedStatement = financialStatementRepository.save(existingStatement);
        log.info("Financial statement updated successfully with ID: {}", updatedStatement.getId());

        return getFinancialStatementById(updatedStatement.getId());
    }

    @Override
    public FinancialStatementResponseDTO getFinancialStatementById(Long id) {
        log.info("Fetching financial statement with ID: {}", id);

        FinancialStatement statement = financialStatementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial statement not found with ID: " + id));

        List<BalanceSheet> balanceSheets = balanceSheetRepository.findByFinancialStatementIdOrderByPeriodOrder(id);
        List<IncomeStatement> incomeStatements = incomeStatementRepository.findByFinancialStatementIdOrderByPeriodOrder(id);

        return buildResponse(statement, balanceSheets, incomeStatements);
    }

    @Override
    public Page<FinancialStatementResponseDTO> getAllFinancialStatements(Pageable pageable) {
        log.info("Fetching all financial statements with pagination");

        return financialStatementRepository.findAll(pageable)
                .map(statement -> {
                    List<BalanceSheet> balanceSheets = balanceSheetRepository
                            .findByFinancialStatementIdOrderByPeriodOrder(statement.getId());
                    List<IncomeStatement> incomeStatements = incomeStatementRepository
                            .findByFinancialStatementIdOrderByPeriodOrder(statement.getId());
                    return buildResponse(statement, balanceSheets, incomeStatements);
                });
    }

    @Override
    public void deleteFinancialStatement(Long id) {
        log.info("Deleting financial statement with ID: {}", id);

        FinancialStatement statement = financialStatementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial statement not found with ID: " + id));

        // Cascade delete will handle related entities
        financialStatementRepository.delete(statement);
        log.info("Financial statement deleted successfully with ID: {}", id);
    }


    @Override
    public FinancialStatementResponseDTO getLatestFinancialStatementByCaseId(String caseId) {
        log.info("Fetching latest financial statement for case: {}", caseId);

        Case case_ = caseRepository.findByCaseId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + caseId));

        FinancialStatement statement = financialStatementRepository.findTopByCaseId_IdOrderByVersionDesc(case_.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No financial statements found for case: " + caseId));

        List<BalanceSheet> balanceSheets = balanceSheetRepository.findByFinancialStatementIdOrderByPeriodOrder(statement.getId());
        List<IncomeStatement> incomeStatements = incomeStatementRepository.findByFinancialStatementIdOrderByPeriodOrder(statement.getId());

        return buildResponse(statement, balanceSheets, incomeStatements);
    }

    @Override
    public FinancialStatementResponseDTO getFinancialStatementByCaseIdAndVersion(String caseId, Integer version) {
        log.info("Fetching financial statement for case: {} with version: {}", caseId, version);

        Case case_ = caseRepository.findByCaseId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + caseId));

        FinancialStatement statement = financialStatementRepository.findByCaseId_IdAndVersion(case_.getId(), version)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Financial statement not found for case: " + caseId + " with version: " + version));

        List<BalanceSheet> balanceSheets = balanceSheetRepository.findByFinancialStatementIdOrderByPeriodOrder(statement.getId());
        List<IncomeStatement> incomeStatements = incomeStatementRepository.findByFinancialStatementIdOrderByPeriodOrder(statement.getId());

        return buildResponse(statement, balanceSheets, incomeStatements);
    }

    @Override
    public List<FinancialStatementResponseDTO> searchByCompanyName(String companyName) {
        log.info("Searching financial statements by company name: {}", companyName);

        List<FinancialStatement> statements = financialStatementRepository.findByCompanyNameContainingIgnoreCase(companyName);

        return statements.stream()
                .map(statement -> {
                    List<BalanceSheet> balanceSheets = balanceSheetRepository
                            .findByFinancialStatementIdOrderByPeriodOrder(statement.getId());
                    List<IncomeStatement> incomeStatements = incomeStatementRepository
                            .findByFinancialStatementIdOrderByPeriodOrder(statement.getId());
                    return buildResponse(statement, balanceSheets, incomeStatements);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<FinancialStatementResponseDTO> getFinancialStatementsByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching financial statements between {} and {}", startDate, endDate);

        List<FinancialStatement> statements = financialStatementRepository.findByReportingDateBetween(startDate, endDate);

        return statements.stream()
                .map(statement -> {
                    List<BalanceSheet> balanceSheets = balanceSheetRepository
                            .findByFinancialStatementIdOrderByPeriodOrder(statement.getId());
                    List<IncomeStatement> incomeStatements = incomeStatementRepository
                            .findByFinancialStatementIdOrderByPeriodOrder(statement.getId());
                    return buildResponse(statement, balanceSheets, incomeStatements);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<FinancialStatementResponseDTO> getFinancialStatementsByType(String statementType) {
        log.info("Fetching financial statements by type: {}", statementType);

        List<FinancialStatement> statements = financialStatementRepository.findByStatementType(statementType);

        return statements.stream()
                .map(statement -> {
                    List<BalanceSheet> balanceSheets = balanceSheetRepository
                            .findByFinancialStatementIdOrderByPeriodOrder(statement.getId());
                    List<IncomeStatement> incomeStatements = incomeStatementRepository
                            .findByFinancialStatementIdOrderByPeriodOrder(statement.getId());
                    return buildResponse(statement, balanceSheets, incomeStatements);
                })
                .collect(Collectors.toList());
    }

    @Override
    public FinancialStatementSummaryDTO getFinancialStatementSummary(Long id) {
        log.info("Generating summary for financial statement ID: {}", id);

        FinancialStatementResponseDTO response = getFinancialStatementById(id);

        List<FinancialStatementResponseDTO.PeriodData> periods = response.getPeriods();

        if (periods.isEmpty()) {
            return buildEmptySummary(response);
        }

        // Calculate key financial highlights
        FinancialStatementResponseDTO.PeriodData latestPeriod = periods.get(periods.size() - 1);
        FinancialStatementResponseDTO.PeriodData previousPeriod = periods.size() > 1 ?
                periods.get(periods.size() - 2) : null;

        BigDecimal totalRevenue = latestPeriod.getSales();
        BigDecimal totalAssets = latestPeriod.getTotalAssets();
        BigDecimal totalLiabilities = latestPeriod.getTotalLiabilities();
        BigDecimal totalEquity = latestPeriod.getTotalCapital();
        BigDecimal netIncome = latestPeriod.getNetIncome();

        // Calculate growth rates
        BigDecimal revenueGrowth = calculateGrowthRate(
                latestPeriod.getSales(),
                previousPeriod != null ? previousPeriod.getSales() : null);

        BigDecimal assetGrowth = calculateGrowthRate(
                latestPeriod.getTotalAssets(),
                previousPeriod != null ? previousPeriod.getTotalAssets() : null);

        BigDecimal incomeGrowth = calculateGrowthRate(
                latestPeriod.getNetIncome(),
                previousPeriod != null ? previousPeriod.getNetIncome() : null);

        // Calculate key ratios
        Map<String, BigDecimal> keyRatios = new HashMap<>();
        keyRatios.put("currentRatio", calculateRatio(latestPeriod.getTotalCurrentAssets(), latestPeriod.getTotalCurrentLiabilities()));
        keyRatios.put("debtToEquity", calculateRatio(totalLiabilities, totalEquity));
        keyRatios.put("returnOnAssets", calculatePercentage(netIncome, totalAssets));
        keyRatios.put("returnOnEquity", calculatePercentage(netIncome, totalEquity));
        keyRatios.put("profitMargin", calculatePercentage(netIncome, totalRevenue));

        // Build trends
        Map<LocalDate, BigDecimal> revenueTrend = periods.stream()
                .collect(Collectors.toMap(
                        FinancialStatementResponseDTO.PeriodData::getPeriodDate,
                        FinancialStatementResponseDTO.PeriodData::getSales,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        Map<LocalDate, BigDecimal> netIncomeTrend = periods.stream()
                .collect(Collectors.toMap(
                        FinancialStatementResponseDTO.PeriodData::getPeriodDate,
                        FinancialStatementResponseDTO.PeriodData::getNetIncome,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        Map<LocalDate, BigDecimal> assetTrend = periods.stream()
                .collect(Collectors.toMap(
                        FinancialStatementResponseDTO.PeriodData::getPeriodDate,
                        FinancialStatementResponseDTO.PeriodData::getTotalAssets,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        // Determine financial health
        String financialHealth = determineFinancialHealth(keyRatios);

        // Generate warnings and strengths
        List<String> warnings = generateWarnings(keyRatios, periods);
        List<String> strengths = generateStrengths(keyRatios, periods);

        return FinancialStatementSummaryDTO.builder()
                .id(id)
                .caseId(response.getCaseId())
                .companyName(response.getCompanyName())
                .reportingDate(response.getReportingDate())
                .version(response.getVersion())
                .totalRevenue(totalRevenue)
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .totalEquity(totalEquity)
                .netIncome(netIncome)
                .revenueGrowth(revenueGrowth)
                .assetGrowth(assetGrowth)
                .incomeGrowth(incomeGrowth)
                .keyRatios(keyRatios)
                .revenueTrend(revenueTrend)
                .netIncomeTrend(netIncomeTrend)
                .assetTrend(assetTrend)
                .totalPeriods(periods.size())
                .oldestPeriod(periods.get(0).getPeriodDate())
                .newestPeriod(latestPeriod.getPeriodDate())
                .financialHealth(financialHealth)
                .warnings(warnings)
                .strengths(strengths)
                .build();
    }

    @Override
    public Map<String, Object> compareFinancialStatements(Long id1, Long id2) {
        log.info("Comparing financial statements ID: {} and ID: {}", id1, id2);

        FinancialStatementResponseDTO fs1 = getFinancialStatementById(id1);
        FinancialStatementResponseDTO fs2 = getFinancialStatementById(id2);

        FinancialStatementSummaryDTO summary1 = getFinancialStatementSummary(id1);
        FinancialStatementSummaryDTO summary2 = getFinancialStatementSummary(id2);

        Map<String, Object> comparison = new HashMap<>();

        // Basic info
        comparison.put("statement1", Map.of(
                "id", fs1.getId(),
                "caseId", fs1.getCaseId(),
                "companyName", fs1.getCompanyName(),
                "version", fs1.getVersion(),
                "reportingDate", fs1.getReportingDate()
        ));

        comparison.put("statement2", Map.of(
                "id", fs2.getId(),
                "caseId", fs2.getCaseId(),
                "companyName", fs2.getCompanyName(),
                "version", fs2.getVersion(),
                "reportingDate", fs2.getReportingDate()
        ));

        // Compare key metrics
        Map<String, Object> metricsComparison = new HashMap<>();
        metricsComparison.put("totalRevenue", Map.of(
                "statement1", summary1.getTotalRevenue(),
                "statement2", summary2.getTotalRevenue(),
                "difference", calculateDifference(summary1.getTotalRevenue(), summary2.getTotalRevenue()),
                "percentageDiff", calculatePercentageDiff(summary1.getTotalRevenue(), summary2.getTotalRevenue())
        ));

        metricsComparison.put("netIncome", Map.of(
                "statement1", summary1.getNetIncome(),
                "statement2", summary2.getNetIncome(),
                "difference", calculateDifference(summary1.getNetIncome(), summary2.getNetIncome()),
                "percentageDiff", calculatePercentageDiff(summary1.getNetIncome(), summary2.getNetIncome())
        ));

        metricsComparison.put("totalAssets", Map.of(
                "statement1", summary1.getTotalAssets(),
                "statement2", summary2.getTotalAssets(),
                "difference", calculateDifference(summary1.getTotalAssets(), summary2.getTotalAssets()),
                "percentageDiff", calculatePercentageDiff(summary1.getTotalAssets(), summary2.getTotalAssets())
        ));

        comparison.put("metricsComparison", metricsComparison);

        // Compare ratios
        Map<String, Object> ratiosComparison = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : summary1.getKeyRatios().entrySet()) {
            String ratioName = entry.getKey();
            BigDecimal ratio1 = entry.getValue();
            BigDecimal ratio2 = summary2.getKeyRatios().get(ratioName);

            ratiosComparison.put(ratioName, Map.of(
                    "statement1", ratio1,
                    "statement2", ratio2,
                    "difference", calculateDifference(ratio1, ratio2),
                    "percentageDiff", calculatePercentageDiff(ratio1, ratio2)
            ));
        }

        comparison.put("ratiosComparison", ratiosComparison);

        // Overall assessment
        String betterPerformer = assessBetterPerformer(summary1, summary2);
        comparison.put("betterPerformer", betterPerformer);

        return comparison;
    }

    @Override
    public Map<String, Object> generateRatioAnalysis(Long id) {
        log.info("Generating ratio analysis for financial statement ID: {}", id);

        FinancialStatementResponseDTO response = getFinancialStatementById(id);

        Map<String, Object> analysis = new HashMap<>();

        // Group ratios by category
        Map<String, List<Map<String, Object>>> categorizedRatios = new HashMap<>();

        for (Map.Entry<String, FinancialStatementResponseDTO.RatioData> entry : response.getRatios().entrySet()) {
            String ratioName = entry.getKey();
            FinancialStatementResponseDTO.RatioData ratioData = entry.getValue();

            String category = determineRatioCategory(ratioName);

            Map<String, Object> ratioInfo = new HashMap<>();
            ratioInfo.put("name", ratioName);
            ratioInfo.put("description", ratioData.getDescription());
            ratioInfo.put("current", getCurrentRatioValue(ratioData));
            ratioInfo.put("average", ratioData.getAverage());
            ratioInfo.put("trend", analyzeTrend(ratioData.getPeriodValues()));
            ratioInfo.put("benchmark", getIndustryBenchmark(ratioName));
            ratioInfo.put("assessment", assessRatio(ratioName, getCurrentRatioValue(ratioData)));

            categorizedRatios.computeIfAbsent(category, k -> new ArrayList<>()).add(ratioInfo);
        }

        analysis.put("categorizedRatios", categorizedRatios);

        // Overall ratio health score
        analysis.put("overallScore", calculateOverallRatioScore(response.getRatios()));
        analysis.put("recommendations", generateRecommendations(response.getRatios()));

        return analysis;
    }

    @Override
    public Map<String, List<BigDecimal>> getTrendAnalysis(Long id, List<String> metrics) {
        log.info("Generating trend analysis for financial statement ID: {} for metrics: {}", id, metrics);

        FinancialStatementResponseDTO response = getFinancialStatementById(id);

        Map<String, List<BigDecimal>> trends = new HashMap<>();

        for (String metric : metrics) {
            switch (metric.toLowerCase()) {
                case "revenue":
                    trends.put(metric, response.getPeriods().stream()
                            .map(FinancialStatementResponseDTO.PeriodData::getSales)
                            .collect(Collectors.toList()));
                    break;
                case "netincome":
                case "net-income":
                case "net_income":
                    trends.put("netIncome", response.getPeriods().stream()
                            .map(FinancialStatementResponseDTO.PeriodData::getNetIncome)
                            .collect(Collectors.toList()));
                    break;
                case "assets":
                    trends.put(metric, response.getPeriods().stream()
                            .map(FinancialStatementResponseDTO.PeriodData::getTotalAssets)
                            .collect(Collectors.toList()));
                    break;
                case "liabilities":
                    trends.put(metric, response.getPeriods().stream()
                            .map(FinancialStatementResponseDTO.PeriodData::getTotalLiabilities)
                            .collect(Collectors.toList()));
                    break;
                case "equity":
                    trends.put(metric, response.getPeriods().stream()
                            .map(FinancialStatementResponseDTO.PeriodData::getTotalCapital)
                            .collect(Collectors.toList()));
                    break;
                default:
                    // Check if it's a ratio
                    if (response.getRatios().containsKey(metric)) {
                        trends.put(metric, response.getRatios().get(metric).getPeriodValues());
                    }
            }
        }

        return trends;
    }

    @Override
    public Map<String, Object> exportFinancialStatementData(Long id) {
        log.info("Preparing financial statement data for export, ID: {}", id);

        FinancialStatementResponseDTO response = getFinancialStatementById(id);
        FinancialStatementSummaryDTO summary = getFinancialStatementSummary(id);

        Map<String, Object> exportData = new HashMap<>();

        // Header information
        exportData.put("statementInfo", Map.of(
                "id", response.getId(),
                "caseId", response.getCaseId(),
                "companyName", response.getCompanyName(),
                "reportingDate", response.getReportingDate(),
                "version", response.getVersion()
        ));

        // Balance Sheet data
        List<Map<String, Object>> balanceSheetData = new ArrayList<>();
        for (FinancialStatementResponseDTO.PeriodData period : response.getPeriods()) {
            Map<String, Object> periodData = new HashMap<>();
            periodData.put("periodDate", period.getPeriodDate());
            periodData.put("isAudited", period.getIsAudited());

            // Assets
            periodData.put("propertyPlantEquipment", period.getPropertyPlantEquipment());
            periodData.put("stocks", period.getStocks());
            periodData.put("tradeOtherReceivables", period.getTradeOtherReceivables());
            periodData.put("cashOnHandBank", period.getCashOnHandBank());
            periodData.put("totalCurrentAssets", period.getTotalCurrentAssets());
            periodData.put("totalAssets", period.getTotalAssets());

            // Liabilities
            periodData.put("tradeOtherPayables", period.getTradeOtherPayables());
            periodData.put("otherPayables", period.getOtherPayables());
            periodData.put("shareholderAccount", period.getShareholderAccount());
            periodData.put("profitTaxPayable", period.getProfitTaxPayable());
            periodData.put("totalCurrentLiabilities", period.getTotalCurrentLiabilities());
            periodData.put("bankLoan", period.getBankLoan());
            periodData.put("leasePayable", period.getLeasePayable());
            periodData.put("totalLiabilities", period.getTotalLiabilities());

            // Equity
            periodData.put("capital", period.getCapital());
            periodData.put("reserve", period.getReserve());
            periodData.put("retainedEarnings", period.getRetainedEarnings());
            periodData.put("totalCapital", period.getTotalCapital());

            balanceSheetData.add(periodData);
        }
        exportData.put("balanceSheet", balanceSheetData);

        // Income Statement data
        List<Map<String, Object>> incomeStatementData = new ArrayList<>();
        for (FinancialStatementResponseDTO.PeriodData period : response.getPeriods()) {
            Map<String, Object> periodData = new HashMap<>();
            periodData.put("periodDate", period.getPeriodDate());
            periodData.put("sales", period.getSales());
            periodData.put("costOfGoodsSold", period.getCostOfGoodsSold());
            periodData.put("grossProfit", period.getGrossProfit());
            periodData.put("operatingExpenses", period.getOperatingExpenses());
            periodData.put("profitBeforeInterestTaxDepreciation", period.getProfitBeforeInterestTaxDepreciation());
            periodData.put("depreciationExpense", period.getDepreciationExpense());
            periodData.put("profitBeforeInterestAndTax", period.getProfitBeforeInterestAndTax());
            periodData.put("interestExpense", period.getInterestExpense());
            periodData.put("profitBeforeTax", period.getProfitBeforeTax());
            periodData.put("incomeTax", period.getIncomeTax());
            periodData.put("netIncome", period.getNetIncome());

            incomeStatementData.add(periodData);
        }
        exportData.put("incomeStatement", incomeStatementData);

        // Averages
        exportData.put("averages", response.getAverages());

        // Ratios
        exportData.put("ratios", response.getRatios());

        // Summary
        exportData.put("summary", summary);

        return exportData;
    }

    @Override
    public Map<String, Object> validateFinancialStatement(Long id) {
        log.info("Validating financial statement ID: {}", id);

        FinancialStatementResponseDTO response = getFinancialStatementById(id);

        Map<String, Object> validationResult = new HashMap<>();
        List<String> discrepancies = new ArrayList<>();
        boolean isValid = true;

        // Check if balance sheet balances for each period
        for (int i = 0; i < response.getPeriods().size(); i++) {
            FinancialStatementResponseDTO.PeriodData period = response.getPeriods().get(i);

            BigDecimal assets = period.getTotalAssets();
            BigDecimal liabilities = period.getTotalLiabilities();
            BigDecimal equity = period.getTotalCapital();
            BigDecimal liabilitiesPlusEquity = liabilities.add(equity);

            if (assets.compareTo(liabilitiesPlusEquity) != 0) {
                isValid = false;
                discrepancies.add(String.format(
                        "Period %d (%s): Assets (%s) ≠ Liabilities (%s) + Equity (%s). Difference: %s",
                        i + 1, period.getPeriodDate(), assets, liabilities, equity,
                        assets.subtract(liabilitiesPlusEquity)));
            }

            // Check if net working capital calculation is consistent
            BigDecimal calculatedNWC = period.getTotalCurrentAssets().subtract(period.getTotalCurrentLiabilities());
            if (period.getNetWorkingCapital().compareTo(calculatedNWC) != 0) {
                discrepancies.add(String.format(
                        "Period %d: Net Working Capital mismatch. Expected: %s, Got: %s",
                        i + 1, calculatedNWC, period.getNetWorkingCapital()));
            }
        }

        // Validate income statement logic
        for (int i = 0; i < response.getPeriods().size(); i++) {
            FinancialStatementResponseDTO.PeriodData period = response.getPeriods().get(i);

            BigDecimal calculatedGrossProfit = period.getSales().subtract(period.getCostOfGoodsSold());
            if (period.getGrossProfit().compareTo(calculatedGrossProfit) != 0) {
                discrepancies.add(String.format(
                        "Period %d: Gross Profit mismatch. Expected: %s, Got: %s",
                        i + 1, calculatedGrossProfit, period.getGrossProfit()));
            }

            // Check if tax calculation is correct (assuming 35% tax rate)
            BigDecimal expectedTax = period.getProfitBeforeTax()
                    .multiply(THIRTY_FIVE_PERCENT)
                    .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
            if (period.getIncomeTax().compareTo(expectedTax) != 0) {
                discrepancies.add(String.format(
                        "Period %d: Income Tax calculation might be incorrect. Expected: %s, Got: %s",
                        i + 1, expectedTax, period.getIncomeTax()));
            }
        }

        validationResult.put("isValid", isValid && discrepancies.isEmpty());
        validationResult.put("discrepancies", discrepancies);
        validationResult.put("discrepancyCount", discrepancies.size());

        return validationResult;
    }

    @Override
    public boolean isBalanceSheetBalanced(Long id) {
        Map<String, Object> validation = validateFinancialStatement(id);
        return (boolean) validation.get("isValid");
    }

    // ============= Helper Methods =============

    private void validateFinancialStatementRequest(FinancialStatementRequestDTO request) {
        if (request.getPeriods() == null || request.getPeriods().isEmpty()) {
            throw new BadRequestException("At least one period is required");
        }

        if (request.getPeriods().size() != 4) {
            throw new BadRequestException("Financial statement requires exactly 4 periods");
        }

        // Check for duplicate period dates
        Set<LocalDate> dates = new HashSet<>();
        for (FinancialStatementRequestDTO.PeriodInput period : request.getPeriods()) {
            if (!dates.add(period.getPeriodDate())) {
                throw new BadRequestException("Duplicate period date found: " + period.getPeriodDate());
            }
        }
    }

    private Integer getNextVersionNumber(Long caseId) {
        return financialStatementRepository.findTopByCaseId_IdOrderByVersionDesc(caseId)
                .map(stat -> stat.getVersion() + 1)
                .orElse(1);
    }

    private void savePeriodsData(FinancialStatement statement, List<FinancialStatementRequestDTO.PeriodInput> periods) {
        for (int i = 0; i < periods.size(); i++) {
            FinancialStatementRequestDTO.PeriodInput period = periods.get(i);

            // Save Balance Sheet
            BalanceSheet balanceSheet = BalanceSheet.builder()
                    .financialStatement(statement)
                    .periodDate(period.getPeriodDate())
                    .isAudited(period.getIsAudited() != null ? period.getIsAudited() : true)
                    .periodOrder(i)
                    .propertyPlantEquipment(period.getPropertyPlantEquipment())
                    .stocks(period.getStocks())
                    .tradeOtherReceivables(period.getTradeOtherReceivables())
                    .cashOnHandBank(period.getCashOnHandBank())
                    .tradeOtherPayables(period.getTradeOtherPayables())
                    .otherPayables(period.getOtherPayables())
                    .shareholderAccount(period.getShareholderAccount())
                    .profitTaxPayable(period.getProfitTaxPayable())
                    .bankLoan(period.getBankLoan())
                    .leasePayable(period.getLeasePayable())
                    .capital(period.getCapital())
                    .reserve(period.getReserve())
                    .retainedEarnings(period.getRetainedEarnings())
                    .build();

            balanceSheetRepository.save(balanceSheet);

            // Save Income Statement
            IncomeStatement incomeStatement = IncomeStatement.builder()
                    .financialStatement(statement)
                    .periodDate(period.getPeriodDate())
                    .isAudited(period.getIsAudited() != null ? period.getIsAudited() : true)
                    .periodOrder(i)
                    .sales(period.getSales() != null ? period.getSales() : BigDecimal.ZERO)
                    .costOfGoodsSold(period.getCostOfGoodsSold() != null ? period.getCostOfGoodsSold() : BigDecimal.ZERO)
                    .operatingExpenses(period.getOperatingExpenses() != null ? period.getOperatingExpenses() : BigDecimal.ZERO)
                    .depreciationExpense(period.getDepreciationExpense() != null ? period.getDepreciationExpense() : BigDecimal.ZERO)
                    .interestExpense(period.getInterestExpense() != null ? period.getInterestExpense() : BigDecimal.ZERO)
//                    .taxRate(period.getTaxRate() != null ? period.getTaxRate() : THIRTY_FIVE_PERCENT)
                    .build();

            incomeStatementRepository.save(incomeStatement);
        }
    }

    private FinancialStatementResponseDTO buildResponse(
            FinancialStatement statement,
            List<BalanceSheet> balanceSheets,
            List<IncomeStatement> incomeStatements) {

        List<FinancialStatementResponseDTO.PeriodData> periods = new ArrayList<>();

        for (int i = 0; i < balanceSheets.size(); i++) {
            BalanceSheet bs = balanceSheets.get(i);
            IncomeStatement is = incomeStatements.get(i);

            FinancialStatementResponseDTO.PeriodData period = FinancialStatementResponseDTO.PeriodData.builder()
                    .periodDate(bs.getPeriodDate())
                    .isAudited(bs.getIsAudited())
                    .periodOrder(bs.getPeriodOrder())

                    // Balance Sheet
                    .propertyPlantEquipment(bs.getPropertyPlantEquipment())
                    .totalNonCurrentAssets(bs.getTotalNonCurrentAssets())
                    .stocks(bs.getStocks())
                    .tradeOtherReceivables(bs.getTradeOtherReceivables())
                    .cashOnHandBank(bs.getCashOnHandBank())
                    .totalCurrentAssets(bs.getTotalCurrentAssets())
                    .totalAssets(bs.getTotalAssets())

                    .tradeOtherPayables(bs.getTradeOtherPayables())
                    .otherPayables(bs.getOtherPayables())
                    .shareholderAccount(bs.getShareholderAccount())
                    .profitTaxPayable(bs.getProfitTaxPayable())
                    .totalCurrentLiabilities(bs.getTotalCurrentLiabilities())
                    .bankLoan(bs.getBankLoan())
                    .leasePayable(bs.getLeasePayable())
                    .totalLongTermLiabilities(bs.getTotalLongTermLiabilities())
                    .totalLiabilities(bs.getTotalLiabilities())

                    .capital(bs.getCapital())
                    .reserve(bs.getReserve())
                    .retainedEarnings(bs.getRetainedEarnings())
                    .totalCapital(bs.getTotalCapital())
                    .totalLiabilitiesAndCapital(bs.getTotalLiabilitiesAndCapital())

                    .netWorkingCapital(bs.getNetWorkingCapital())
                    .tangibleNetWorth(bs.getTangibleNetWorth())

                    // Income Statement
                    .sales(is.getSales())
                    .costOfGoodsSold(is.getCostOfGoodsSold())
                    .grossProfit(is.getGrossProfit())
                    .operatingExpenses(is.getOperatingExpenses())
                    .profitBeforeInterestTaxDepreciation(is.getProfitBeforeInterestTaxDepreciation())
                    .depreciationExpense(is.getDepreciationExpense())
                    .profitBeforeInterestAndTax(is.getProfitBeforeInterestAndTax())
                    .interestExpense(is.getInterestExpense())
                    .profitBeforeTax(is.getProfitBeforeTax())
                    .incomeTax(is.getIncomeTax())
                    .netIncome(is.getNetIncome())
                    .build();

            periods.add(period);
        }

        // Calculate averages
        FinancialStatementResponseDTO.PeriodData averages = calculateAverages(periods);

        // Calculate ratios
        Map<String, FinancialStatementResponseDTO.RatioData> ratios =
                calculateRatios(balanceSheets, incomeStatements);

        return FinancialStatementResponseDTO.builder()
                .id(statement.getId())
                .caseId(statement.getCaseId().getCaseId())
                .companyName(statement.getCompanyName())
                .reportingDate(statement.getReportingDate())
                .version(statement.getVersion())
                .periods(periods)
                .averages(averages)
                .ratios(ratios)
                .build();
    }

    private FinancialStatementResponseDTO.PeriodData calculateAverages(
            List<FinancialStatementResponseDTO.PeriodData> periods) {

        FinancialStatementResponseDTO.PeriodData averages = new FinancialStatementResponseDTO.PeriodData();

        // Balance Sheet averages
        averages.setPropertyPlantEquipment(averageField(periods, p -> p.getPropertyPlantEquipment()));
        averages.setStocks(averageField(periods, p -> p.getStocks()));
        averages.setTradeOtherReceivables(averageField(periods, p -> p.getTradeOtherReceivables()));
        averages.setCashOnHandBank(averageField(periods, p -> p.getCashOnHandBank()));
        averages.setTradeOtherPayables(averageField(periods, p -> p.getTradeOtherPayables()));
        averages.setOtherPayables(averageField(periods, p -> p.getOtherPayables()));
        averages.setShareholderAccount(averageField(periods, p -> p.getShareholderAccount()));
        averages.setProfitTaxPayable(averageField(periods, p -> p.getProfitTaxPayable()));
        averages.setBankLoan(averageField(periods, p -> p.getBankLoan()));
        averages.setLeasePayable(averageField(periods, p -> p.getLeasePayable()));
        averages.setCapital(averageField(periods, p -> p.getCapital()));
        averages.setReserve(averageField(periods, p -> p.getReserve()));
        averages.setRetainedEarnings(averageField(periods, p -> p.getRetainedEarnings()));

        // Income Statement averages
        averages.setSales(averageField(periods, p -> p.getSales()));
        averages.setCostOfGoodsSold(averageField(periods, p -> p.getCostOfGoodsSold()));
        averages.setOperatingExpenses(averageField(periods, p -> p.getOperatingExpenses()));
        averages.setDepreciationExpense(averageField(periods, p -> p.getDepreciationExpense()));
        averages.setInterestExpense(averageField(periods, p -> p.getInterestExpense()));

        return averages;
    }

    private BigDecimal averageField(
            List<FinancialStatementResponseDTO.PeriodData> periods,
            java.util.function.Function<FinancialStatementResponseDTO.PeriodData, BigDecimal> extractor) {

        return periods.stream()
                .map(extractor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(periods.size()), 2, RoundingMode.HALF_UP);
    }

    private Map<String, FinancialStatementResponseDTO.RatioData> calculateRatios(
            List<BalanceSheet> balanceSheets,
            List<IncomeStatement> incomeStatements) {

        Map<String, FinancialStatementResponseDTO.RatioData> ratios = new LinkedHashMap<>();

        // Calculate ratios for each period
        List<FinancialRatioDTO> periodRatios = new ArrayList<>();
        for (int i = 0; i < balanceSheets.size(); i++) {
            BalanceSheet bs = balanceSheets.get(i);
            IncomeStatement is = incomeStatements.get(i);
            IncomeStatement previousIs = i > 0 ? incomeStatements.get(i - 1) : null;

            FinancialRatioDTO ratio = ratioCalculator.calculateAllRatios(bs, is, previousIs);
            periodRatios.add(ratio);
        }

        // Add each ratio type to response
        addRatioToMap(ratios, "Activity Ratio (Sales Growth)",
                "Year-over-year sales growth rate", periodRatios, FinancialRatioDTO::getActivityRatio);
        addRatioToMap(ratios, "Inventory Turnover",
                "How many times inventory is sold and replaced", periodRatios, FinancialRatioDTO::getInventoryTurnover);
        addRatioToMap(ratios, "Collection Period (days)",
                "Average days to collect receivables", periodRatios, FinancialRatioDTO::getCollectionPeriod);
        addRatioToMap(ratios, "Working Capital to Sales",
                "Working capital efficiency", periodRatios, FinancialRatioDTO::getWorkingCapitalToSales);
        addRatioToMap(ratios, "Current Ratio",
                "Ability to pay short-term obligations", periodRatios, FinancialRatioDTO::getCurrentRatio);
        addRatioToMap(ratios, "Acid Test Ratio",
                "Ability to pay immediate obligations", periodRatios, FinancialRatioDTO::getAcidTestRatio);
        addRatioToMap(ratios, "Equity to Total Asset (%)",
                "Financial leverage", periodRatios, FinancialRatioDTO::getEquityToTotalAsset);
        addRatioToMap(ratios, "Debt to Total Asset (%)",
                "Percentage of assets financed by debt", periodRatios, FinancialRatioDTO::getDebtToTotalAsset);
        addRatioToMap(ratios, "Debt to Net Worth",
                "Leverage ratio", periodRatios, FinancialRatioDTO::getDebtToNetWorth);
        addRatioToMap(ratios, "ROA (%)",
                "Return on Assets", periodRatios, FinancialRatioDTO::getRoa);
        addRatioToMap(ratios, "ROE (%)",
                "Return on Equity", periodRatios, FinancialRatioDTO::getRoe);
        addRatioToMap(ratios, "Debt Service Coverage",
                "Ability to pay interest", periodRatios, FinancialRatioDTO::getDebtServiceCoverageRatio);
        addRatioToMap(ratios, "Operating Profit Margin (%)",
                "Operating profitability", periodRatios, FinancialRatioDTO::getOperatingProfitMargin);
        addRatioToMap(ratios, "Gross Profit Margin (%)",
                "Gross profitability", periodRatios, FinancialRatioDTO::getGrossProfitMargin);
        addRatioToMap(ratios, "Net Profit Margin (%)",
                "Net profitability", periodRatios, FinancialRatioDTO::getNetProfitMargin);

        return ratios;
    }

    private void addRatioToMap(
            Map<String, FinancialStatementResponseDTO.RatioData> map,
            String name,
            String description,
            List<FinancialRatioDTO> periodRatios,
            java.util.function.Function<FinancialRatioDTO, BigDecimal> extractor) {

        List<BigDecimal> values = periodRatios.stream()
                .map(extractor)
                .collect(Collectors.toList());

        BigDecimal average = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);

        FinancialStatementResponseDTO.RatioData ratioData = FinancialStatementResponseDTO.RatioData.builder()
                .name(name)
                .description(description)
                .periodValues(values)
                .average(average)
                .build();

        // Set the specific ratio value based on name
        switch (name) {
            case "Activity Ratio (Sales Growth)":
                ratioData.setActivityRatio(values.get(values.size() - 1));
                break;
            case "Inventory Turnover":
                ratioData.setInventoryTurnover(values.get(values.size() - 1));
                break;
            case "Collection Period (days)":
                ratioData.setCollectionPeriod(values.get(values.size() - 1));
                break;
            case "Working Capital to Sales":
                ratioData.setWorkingCapitalToSales(values.get(values.size() - 1));
                break;
            case "Current Ratio":
                ratioData.setCurrentRatio(values.get(values.size() - 1));
                break;
            case "Acid Test Ratio":
                ratioData.setAcidTestRatio(values.get(values.size() - 1));
                break;
            case "Equity to Total Asset (%)":
                ratioData.setEquityToTotalAsset(values.get(values.size() - 1));
                break;
            case "Debt to Total Asset (%)":
                ratioData.setDebtToTotalAsset(values.get(values.size() - 1));
                break;
            case "Debt to Net Worth":
                ratioData.setDebtToNetWorth(values.get(values.size() - 1));
                break;
            case "ROA (%)":
                ratioData.setRoa(values.get(values.size() - 1));
                break;
            case "ROE (%)":
                ratioData.setRoe(values.get(values.size() - 1));
                break;
            case "Debt Service Coverage":
                ratioData.setDebtServiceCoverageRatio(values.get(values.size() - 1));
                break;
            case "Operating Profit Margin (%)":
                ratioData.setOperatingProfitMargin(values.get(values.size() - 1));
                break;
            case "Gross Profit Margin (%)":
                ratioData.setGrossProfitMargin(values.get(values.size() - 1));
                break;
            case "Net Profit Margin (%)":
                ratioData.setNetProfitMargin(values.get(values.size() - 1));
                break;
        }

        map.put(name, ratioData);
    }

    private FinancialStatementSummaryDTO buildEmptySummary(FinancialStatementResponseDTO response) {
        return FinancialStatementSummaryDTO.builder()
                .id(response.getId())
                .caseId(response.getCaseId())
                .companyName(response.getCompanyName())
                .reportingDate(response.getReportingDate())
                .version(response.getVersion())
                .totalRevenue(BigDecimal.ZERO)
                .totalAssets(BigDecimal.ZERO)
                .totalLiabilities(BigDecimal.ZERO)
                .totalEquity(BigDecimal.ZERO)
                .netIncome(BigDecimal.ZERO)
                .revenueGrowth(BigDecimal.ZERO)
                .assetGrowth(BigDecimal.ZERO)
                .incomeGrowth(BigDecimal.ZERO)
                .keyRatios(new HashMap<>())
                .revenueTrend(new LinkedHashMap<>())
                .netIncomeTrend(new LinkedHashMap<>())
                .assetTrend(new LinkedHashMap<>())
                .totalPeriods(0)
                .oldestPeriod(null)
                .newestPeriod(null)
                .financialHealth("NO_DATA")
                .warnings(new ArrayList<>())
                .strengths(new ArrayList<>())
                .build();
    }

    private BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
    }

    private BigDecimal calculateRatio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePercentage(BigDecimal part, BigDecimal whole) {
        if (whole == null || whole.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return part.multiply(ONE_HUNDRED)
                .divide(whole, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDifference(BigDecimal val1, BigDecimal val2) {
        if (val1 == null || val2 == null) {
            return BigDecimal.ZERO;
        }
        return val1.subtract(val2);
    }

    private BigDecimal calculatePercentageDiff(BigDecimal val1, BigDecimal val2) {
        if (val2 == null || val2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return val1.subtract(val2)
                .multiply(ONE_HUNDRED)
                .divide(val2, 2, RoundingMode.HALF_UP);
    }

    private String determineFinancialHealth(Map<String, BigDecimal> ratios) {
        BigDecimal currentRatio = ratios.getOrDefault("currentRatio", BigDecimal.ZERO);
        BigDecimal debtToEquity = ratios.getOrDefault("debtToEquity", BigDecimal.ZERO);
        BigDecimal profitMargin = ratios.getOrDefault("profitMargin", BigDecimal.ZERO);

        if (currentRatio.compareTo(new BigDecimal("2")) >= 0 &&
                debtToEquity.compareTo(new BigDecimal("1")) <= 0 &&
                profitMargin.compareTo(BigDecimal.ZERO) > 0) {
            return "HEALTHY";
        } else if (currentRatio.compareTo(new BigDecimal("1")) >= 0 &&
                debtToEquity.compareTo(new BigDecimal("2")) <= 0) {
            return "MODERATE";
        } else {
            return "WEAK";
        }
    }

    private List<String> generateWarnings(Map<String, BigDecimal> ratios,
                                          List<FinancialStatementResponseDTO.PeriodData> periods) {
        List<String> warnings = new ArrayList<>();

        BigDecimal currentRatio = ratios.getOrDefault("currentRatio", BigDecimal.ZERO);
        if (currentRatio.compareTo(new BigDecimal("1")) < 0) {
            warnings.add("Current ratio is below 1, indicating potential liquidity issues");
        }

        BigDecimal debtToEquity = ratios.getOrDefault("debtToEquity", BigDecimal.ZERO);
        if (debtToEquity.compareTo(new BigDecimal("2")) > 0) {
            warnings.add("High debt-to-equity ratio, indicating significant leverage");
        }

        BigDecimal profitMargin = ratios.getOrDefault("profitMargin", BigDecimal.ZERO);
        if (profitMargin.compareTo(BigDecimal.ZERO) <= 0) {
            warnings.add("Negative or zero profit margin");
        }

        // Check for declining revenue
        if (periods.size() >= 2) {
            BigDecimal firstRevenue = periods.get(0).getSales();
            BigDecimal lastRevenue = periods.get(periods.size() - 1).getSales();
            if (lastRevenue.compareTo(firstRevenue) < 0) {
                warnings.add("Revenue is declining over the periods");
            }
        }

        return warnings;
    }

    private List<String> generateStrengths(Map<String, BigDecimal> ratios,
                                           List<FinancialStatementResponseDTO.PeriodData> periods) {
        List<String> strengths = new ArrayList<>();

        BigDecimal currentRatio = ratios.getOrDefault("currentRatio", BigDecimal.ZERO);
        if (currentRatio.compareTo(new BigDecimal("2")) >= 0) {
            strengths.add("Strong liquidity position with current ratio above 2");
        }

        BigDecimal debtToEquity = ratios.getOrDefault("debtToEquity", BigDecimal.ZERO);
        if (debtToEquity.compareTo(new BigDecimal("1")) <= 0) {
            strengths.add("Conservative leverage with debt-to-equity below 1");
        }

        BigDecimal profitMargin = ratios.getOrDefault("profitMargin", BigDecimal.ZERO);
        if (profitMargin.compareTo(new BigDecimal("10")) > 0) {
            strengths.add("Strong profit margin above 10%");
        }

        BigDecimal roe = ratios.getOrDefault("returnOnEquity", BigDecimal.ZERO);
        if (roe.compareTo(new BigDecimal("15")) > 0) {
            strengths.add("Excellent return on equity above 15%");
        }

        // Check for consistent growth
        if (periods.size() >= 2) {
            boolean consistentGrowth = true;
            BigDecimal prevRevenue = null;
            for (FinancialStatementResponseDTO.PeriodData period : periods) {
                if (prevRevenue != null && period.getSales().compareTo(prevRevenue) < 0) {
                    consistentGrowth = false;
                    break;
                }
                prevRevenue = period.getSales();
            }
            if (consistentGrowth) {
                strengths.add("Consistent revenue growth across all periods");
            }
        }

        return strengths;
    }

    private String determineRatioCategory(String ratioName) {
        if (ratioName.contains("Activity") || ratioName.contains("Turnover") ||
                ratioName.contains("Collection") || ratioName.contains("Working Capital")) {
            return "Efficiency Ratios";
        } else if (ratioName.contains("Current") || ratioName.contains("Acid") ||
                ratioName.contains("Equity") || ratioName.contains("Debt")) {
            return "Financial Strength Ratios";
        } else if (ratioName.contains("ROA") || ratioName.contains("ROE") ||
                ratioName.contains("Margin") || ratioName.contains("Profit")) {
            return "Profitability Ratios";
        } else {
            return "Other Ratios";
        }
    }

    private BigDecimal getCurrentRatioValue(FinancialStatementResponseDTO.RatioData ratioData) {
        List<BigDecimal> values = ratioData.getPeriodValues();
        return values.isEmpty() ? BigDecimal.ZERO : values.get(values.size() - 1);
    }

    private String analyzeTrend(List<BigDecimal> values) {
        if (values.size() < 2) {
            return "STABLE";
        }

        BigDecimal first = values.get(0);
        BigDecimal last = values.get(values.size() - 1);

        if (last.compareTo(first) > 0) {
            return "IMPROVING";
        } else if (last.compareTo(first) < 0) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }

    private BigDecimal getIndustryBenchmark(String ratioName) {
        // This would typically come from a configuration or database
        Map<String, BigDecimal> benchmarks = new HashMap<>();
        benchmarks.put("Current Ratio", new BigDecimal("2"));
        benchmarks.put("Acid Test Ratio", new BigDecimal("1"));
        benchmarks.put("Debt to Equity", new BigDecimal("1"));
        benchmarks.put("ROA", new BigDecimal("5"));
        benchmarks.put("ROE", new BigDecimal("12"));
        benchmarks.put("Profit Margin", new BigDecimal("8"));

        return benchmarks.getOrDefault(ratioName, BigDecimal.ONE);
    }

    private String assessRatio(String ratioName, BigDecimal value) {
        BigDecimal benchmark = getIndustryBenchmark(ratioName);

        if (value.compareTo(benchmark.multiply(new BigDecimal("1.2"))) > 0) {
            return "EXCELLENT";
        } else if (value.compareTo(benchmark) >= 0) {
            return "GOOD";
        } else if (value.compareTo(benchmark.multiply(new BigDecimal("0.8"))) >= 0) {
            return "FAIR";
        } else {
            return "POOR";
        }
    }

    private Integer calculateOverallRatioScore(Map<String, FinancialStatementResponseDTO.RatioData> ratios) {
        int totalScore = 0;
        int count = 0;

        for (Map.Entry<String, FinancialStatementResponseDTO.RatioData> entry : ratios.entrySet()) {
            String ratioName = entry.getKey();
            BigDecimal value = getCurrentRatioValue(entry.getValue());

            String assessment = assessRatio(ratioName, value);
            switch (assessment) {
                case "EXCELLENT":
                    totalScore += 100;
                    break;
                case "GOOD":
                    totalScore += 75;
                    break;
                case "FAIR":
                    totalScore += 50;
                    break;
                case "POOR":
                    totalScore += 25;
                    break;
                default:
                    totalScore += 0;
            }
            count++;
        }

        return count > 0 ? totalScore / count : 0;
    }

    private List<String> generateRecommendations(Map<String, FinancialStatementResponseDTO.RatioData> ratios) {
        List<String> recommendations = new ArrayList<>();

        for (Map.Entry<String, FinancialStatementResponseDTO.RatioData> entry : ratios.entrySet()) {
            String ratioName = entry.getKey();
            BigDecimal value = getCurrentRatioValue(entry.getValue());
            String assessment = assessRatio(ratioName, value);

            if ("POOR".equals(assessment)) {
                switch (ratioName) {
                    case "Current Ratio":
                        recommendations.add("Improve current ratio by increasing current assets or reducing short-term liabilities");
                        break;
                    case "Debt to Equity":
                        recommendations.add("Reduce debt levels or increase equity to improve leverage");
                        break;
                    case "Profit Margin":
                        recommendations.add("Focus on cost reduction or pricing strategies to improve profitability");
                        break;
                    case "Inventory Turnover":
                        recommendations.add("Optimize inventory management to improve turnover");
                        break;
                    case "Collection Period":
                        recommendations.add("Implement stricter credit policies to reduce collection period");
                        break;
                }
            }
        }

        return recommendations;
    }

    private String assessBetterPerformer(FinancialStatementSummaryDTO summary1,
                                         FinancialStatementSummaryDTO summary2) {
        int score1 = calculatePerformanceScore(summary1);
        int score2 = calculatePerformanceScore(summary2);

        if (score1 > score2) {
            return "STATEMENT_1";
        } else if (score2 > score1) {
            return "STATEMENT_2";
        } else {
            return "EQUAL";
        }
    }

    private int calculatePerformanceScore(FinancialStatementSummaryDTO summary) {
        int score = 0;

        // Revenue growth
        if (summary.getRevenueGrowth().compareTo(new BigDecimal("10")) > 0) score += 20;
        else if (summary.getRevenueGrowth().compareTo(BigDecimal.ZERO) > 0) score += 10;

        // Profitability
        if (summary.getNetIncome().compareTo(BigDecimal.ZERO) > 0) score += 20;

        // Key ratios
        Map<String, BigDecimal> ratios = summary.getKeyRatios();
        if (ratios.getOrDefault("currentRatio", BigDecimal.ZERO).compareTo(new BigDecimal("2")) > 0) score += 15;
        if (ratios.getOrDefault("debtToEquity", BigDecimal.ZERO).compareTo(new BigDecimal("1")) < 0) score += 15;
        if (ratios.getOrDefault("profitMargin", BigDecimal.ZERO).compareTo(new BigDecimal("10")) > 0) score += 15;
        if (ratios.getOrDefault("returnOnEquity", BigDecimal.ZERO).compareTo(new BigDecimal("15")) > 0) score += 15;

        return score;
    }
}