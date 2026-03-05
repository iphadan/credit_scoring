package com.cbo.credit_scoring.services.impls;

import com.cbo.credit_scoring.dtos.AccountStatementDTO;
import com.cbo.credit_scoring.models.AccountStatement;
import com.cbo.credit_scoring.models.AccountStatementHeader;
import com.cbo.credit_scoring.repositories.AccountStatementRepository;
import com.cbo.credit_scoring.repositories.AccountStatementHeaderRepository;
import com.cbo.credit_scoring.services.AccountStatementService;
import com.cbo.credit_scoring.exceptions.ResourceNotFoundException;
import com.cbo.credit_scoring.exceptions.BadRequestException;
import com.cbo.credit_scoring.exceptions.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountStatementServiceImpl implements AccountStatementService {

    private final AccountStatementRepository statementRepository;
    private final AccountStatementHeaderRepository headerRepository;

    @Override
    public AccountStatementDTO createStatement(AccountStatementDTO statementDTO) {
        log.info("Creating new account statement record");

        if (statementDTO.getHeaderId() == null) {
            throw new BadRequestException("Header ID is required");
        }

        // Find header
        AccountStatementHeader header = headerRepository.findById(statementDTO.getHeaderId())
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + statementDTO.getHeaderId()));

        // Validate statement data
        validateStatement(statementDTO);

        // Check for duplicate month
        if (statementDTO.getMonth() != null) {
            boolean monthExists = header.getStatementRecords().stream()
                    .anyMatch(s -> s.getMonth() != null && s.getMonth().equals(statementDTO.getMonth()));

            if (monthExists) {
                throw new DuplicateResourceException("Statement record for month " + statementDTO.getMonth() + " already exists");
            }
        }

        // Create new statement record
        AccountStatement statement = new AccountStatement();
        statement.setMonth(statementDTO.getMonth());
        statement.setTotalTurnoverCredit(statementDTO.getTotalTurnoverCredit() != null ?
                statementDTO.getTotalTurnoverCredit() : BigDecimal.ZERO);
        statement.setTotalTurnoverDebit(statementDTO.getTotalTurnoverDebit() != null ?
                statementDTO.getTotalTurnoverDebit() : BigDecimal.ZERO);
        statement.setNumberOfCreditEntries(statementDTO.getNumberOfCreditEntries() != null ?
                statementDTO.getNumberOfCreditEntries() : 0);

        // Calculate derived fields
        statement.calculateMonthlyCreditAverage();
        statement.calculateUtilizationPercentage(header.getSanctionLimit());
        statement.setHeader(header);

        // Save statement
        AccountStatement savedStatement = statementRepository.save(statement);
        log.info("Account statement record created successfully with ID: {}", savedStatement.getId());

        return mapToDTO(savedStatement);
    }

    @Override
    public AccountStatementDTO updateStatement(Long id, AccountStatementDTO statementDTO) {
        log.info("Updating account statement record with ID: {}", id);

        // Find existing statement
        AccountStatement existingStatement = statementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Statement record not found with ID: " + id));

        // Validate statement data
        validateStatement(statementDTO);

        // Check for duplicate month if month is being changed
        if (statementDTO.getMonth() != null && !statementDTO.getMonth().equals(existingStatement.getMonth())) {
            boolean monthExists = existingStatement.getHeader().getStatementRecords().stream()
                    .filter(s -> !s.getId().equals(id))
                    .anyMatch(s -> s.getMonth() != null && s.getMonth().equals(statementDTO.getMonth()));

            if (monthExists) {
                throw new DuplicateResourceException("Statement record for month " + statementDTO.getMonth() + " already exists");
            }
        }

        // Update fields
        if (statementDTO.getMonth() != null) existingStatement.setMonth(statementDTO.getMonth());
        if (statementDTO.getTotalTurnoverCredit() != null) existingStatement.setTotalTurnoverCredit(statementDTO.getTotalTurnoverCredit());
        if (statementDTO.getTotalTurnoverDebit() != null) existingStatement.setTotalTurnoverDebit(statementDTO.getTotalTurnoverDebit());
        if (statementDTO.getNumberOfCreditEntries() != null) existingStatement.setNumberOfCreditEntries(statementDTO.getNumberOfCreditEntries());

        // Recalculate derived fields
        existingStatement.calculateMonthlyCreditAverage();
        existingStatement.calculateUtilizationPercentage(existingStatement.getHeader().getSanctionLimit());

        // Save updated statement
        AccountStatement updatedStatement = statementRepository.save(existingStatement);
        log.info("Account statement record updated successfully with ID: {}", updatedStatement.getId());

        return mapToDTO(updatedStatement);
    }

    @Override
    public AccountStatementDTO getStatementById(Long id) {
        log.info("Fetching account statement record with ID: {}", id);

        AccountStatement statement = statementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Statement record not found with ID: " + id));

        return mapToDTO(statement);
    }

    @Override
    public Page<AccountStatementDTO> getAllStatements(Pageable pageable) {
        log.info("Fetching all account statement records with pagination");

        return statementRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public void deleteStatement(Long id) {
        log.info("Deleting account statement record with ID: {}", id);

        AccountStatement statement = statementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Statement record not found with ID: " + id));

        statementRepository.delete(statement);
        log.info("Account statement record deleted successfully with ID: {}", id);
    }

    @Override
    public List<AccountStatementDTO> getStatementsByHeaderId(Long headerId) {
        log.info("Fetching statement records for header ID: {}", headerId);

        if (!headerRepository.existsById(headerId)) {
            throw new ResourceNotFoundException("Header not found with ID: " + headerId);
        }

        return statementRepository.findByHeaderId(headerId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountStatementDTO> getStatementsByHeaderIdOrdered(Long headerId, boolean ascending) {
        log.info("Fetching statement records for header ID: {} ordered {}", headerId, ascending ? "ASC" : "DESC");

        if (!headerRepository.existsById(headerId)) {
            throw new ResourceNotFoundException("Header not found with ID: " + headerId);
        }

        List<AccountStatement> statements = ascending ?
                statementRepository.findByHeaderIdOrderByMonthAsc(headerId) :
                statementRepository.findByHeaderIdOrderByMonthDesc(headerId);

        return statements.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountStatementDTO> getStatementsByMonth(YearMonth month) {
        log.info("Fetching statement records for month: {}", month);

        return statementRepository.findByMonth(month)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountStatementDTO> getStatementsByMonthRange(YearMonth startMonth, YearMonth endMonth) {
        log.info("Fetching statement records between {} and {}", startMonth, endMonth);

        return statementRepository.findByMonthBetween(startMonth, endMonth)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountStatementDTO getStatementByHeaderIdAndMonth(Long headerId, YearMonth month) {
        log.info("Fetching statement record for header ID: {} and month: {}", headerId, month);

        AccountStatement statement = statementRepository.findByHeaderIdAndMonth(headerId, month)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Statement record not found for header ID: " + headerId + " and month: " + month));

        return mapToDTO(statement);
    }

    @Override
    public Map<YearMonth, BigDecimal> getMonthlyCreditTrend(Long headerId) {
        log.info("Generating monthly credit trend for header ID: {}", headerId);

        List<AccountStatement> statements = statementRepository.findByHeaderIdOrderByMonthAsc(headerId);

        return statements.stream()
                .filter(s -> s.getMonth() != null && s.getTotalTurnoverCredit() != null)
                .collect(Collectors.toMap(
                        AccountStatement::getMonth,
                        AccountStatement::getTotalTurnoverCredit,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));
    }

    @Override
    public Map<YearMonth, BigDecimal> getMonthlyDebitTrend(Long headerId) {
        log.info("Generating monthly debit trend for header ID: {}", headerId);

        List<AccountStatement> statements = statementRepository.findByHeaderIdOrderByMonthAsc(headerId);

        return statements.stream()
                .filter(s -> s.getMonth() != null && s.getTotalTurnoverDebit() != null)
                .collect(Collectors.toMap(
                        AccountStatement::getMonth,
                        AccountStatement::getTotalTurnoverDebit,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));
    }

    @Override
    public Map<YearMonth, BigDecimal> getMonthlyUtilizationTrend(Long headerId) {
        log.info("Generating monthly utilization trend for header ID: {}", headerId);

        List<AccountStatement> statements = statementRepository.findByHeaderIdOrderByMonthAsc(headerId);

        return statements.stream()
                .filter(s -> s.getMonth() != null && s.getUtilizationPercentage() != null)
                .collect(Collectors.toMap(
                        AccountStatement::getMonth,
                        AccountStatement::getUtilizationPercentage,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));
    }

    @Override
    public Map<YearMonth, BigDecimal> getMonthlyNetTurnoverTrend(Long headerId) {
        log.info("Generating monthly net turnover trend for header ID: {}", headerId);

        List<AccountStatement> statements = statementRepository.findByHeaderIdOrderByMonthAsc(headerId);

        return statements.stream()
                .filter(s -> s.getMonth() != null)
                .collect(Collectors.toMap(
                        AccountStatement::getMonth,
                        s -> s.calculateNetTurnover(),
                        (v1, v2) -> v1,
                        TreeMap::new
                ));
    }

    @Override
    public AccountStatementDTO getMonthWithHighestUtilization(Long headerId) {
        log.info("Finding month with highest utilization for header ID: {}", headerId);

        Optional<AccountStatement> statement = statementRepository.findMonthWithHighestUtilization(headerId);

        return statement.map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No statement records found for header ID: " + headerId));
    }

    @Override
    public AccountStatementDTO getMonthWithLowestUtilization(Long headerId) {
        log.info("Finding month with lowest utilization for header ID: {}", headerId);

        Optional<AccountStatement> statement = statementRepository.findMonthWithLowestUtilization(headerId);

        return statement.map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No statement records found for header ID: " + headerId));
    }

    @Override
    public AccountStatementDTO getMonthWithHighestCredit(Long headerId) {
        log.info("Finding month with highest credit for header ID: {}", headerId);

        Optional<AccountStatement> statement = statementRepository.findMonthWithHighestCredit(headerId);

        return statement.map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No statement records found for header ID: " + headerId));
    }

    @Override
    public AccountStatementDTO getMonthWithHighestDebit(Long headerId) {
        log.info("Finding month with highest debit for header ID: {}", headerId);

        Optional<AccountStatement> statement = statementRepository.findMonthWithHighestDebit(headerId);

        return statement.map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No statement records found for header ID: " + headerId));
    }

    @Override
    public AccountStatementDTO getLatestStatement(Long headerId) {
        log.info("Finding latest statement for header ID: {}", headerId);

        Optional<AccountStatement> statement = statementRepository.findLatestByHeaderId(headerId);

        return statement.map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No statement records found for header ID: " + headerId));
    }

    @Override
    public AccountStatementDTO getOldestStatement(Long headerId) {
        log.info("Finding oldest statement for header ID: {}", headerId);

        Optional<AccountStatement> statement = statementRepository.findOldestByHeaderId(headerId);

        return statement.map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No statement records found for header ID: " + headerId));
    }

    @Override
    public List<AccountStatementDTO> getHighUtilizationMonths(Long headerId, BigDecimal threshold) {
        log.info("Fetching high utilization months for header ID: {} with threshold: {}", headerId, threshold);

        return statementRepository.findByHeaderId(headerId).stream()
                .filter(s -> s.getUtilizationPercentage() != null &&
                        s.getUtilizationPercentage().compareTo(threshold) > 0)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountStatementDTO> getLowUtilizationMonths(Long headerId, BigDecimal threshold) {
        log.info("Fetching low utilization months for header ID: {} with threshold: {}", headerId, threshold);

        return statementRepository.findByHeaderId(headerId).stream()
                .filter(s -> s.getUtilizationPercentage() != null &&
                        s.getUtilizationPercentage().compareTo(threshold) < 0)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStatementSummary(Long headerId) {
        log.info("Generating statement summary for header ID: {}", headerId);

        BigDecimal totalCredit = statementRepository.sumTotalCreditByHeaderId(headerId);
        BigDecimal totalDebit = statementRepository.sumTotalDebitByHeaderId(headerId);
        BigDecimal avgUtilization = statementRepository.averageUtilizationByHeaderId(headerId);
        BigDecimal maxUtilization = statementRepository.maxUtilizationByHeaderId(headerId);
        BigDecimal minUtilization = statementRepository.minUtilizationByHeaderId(headerId);
        Long recordCount = statementRepository.countByHeaderId(headerId);

        Optional<AccountStatement> latest = statementRepository.findLatestByHeaderId(headerId);
        Optional<AccountStatement> highest = statementRepository.findMonthWithHighestUtilization(headerId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("headerId", headerId);
        summary.put("totalCredit", totalCredit);
        summary.put("totalDebit", totalDebit);
        summary.put("netTurnover", totalCredit.subtract(totalDebit));
        summary.put("averageUtilization", avgUtilization);
        summary.put("maxUtilization", maxUtilization);
        summary.put("minUtilization", minUtilization);
        summary.put("totalMonths", recordCount);
        summary.put("latestMonth", latest.map(AccountStatement::getMonth).orElse(null));
        summary.put("latestUtilization", latest.map(AccountStatement::getUtilizationPercentage).orElse(null));
        summary.put("highestUtilizationMonth", highest.map(AccountStatement::getMonth).orElse(null));
        summary.put("highestUtilizationValue", highest.map(AccountStatement::getUtilizationPercentage).orElse(null));

        return summary;
    }

    @Override
    public Map<String, Object> getComparativeAnalysis(Long headerId1, Long headerId2) {
        log.info("Performing comparative analysis for header IDs: {} and {}", headerId1, headerId2);

        Map<String, Object> analysis = new HashMap<>();

        // Get summaries for both headers
        Map<String, Object> summary1 = getStatementSummary(headerId1);
        Map<String, Object> summary2 = getStatementSummary(headerId2);

        analysis.put("header1", summary1);
        analysis.put("header2", summary2);

        // Calculate comparisons
        BigDecimal totalCredit1 = (BigDecimal) summary1.get("totalCredit");
        BigDecimal totalCredit2 = (BigDecimal) summary2.get("totalCredit");
        BigDecimal creditDiff = totalCredit1.subtract(totalCredit2);

        BigDecimal totalDebit1 = (BigDecimal) summary1.get("totalDebit");
        BigDecimal totalDebit2 = (BigDecimal) summary2.get("totalDebit");
        BigDecimal debitDiff = totalDebit1.subtract(totalDebit2);

        BigDecimal avgUtil1 = (BigDecimal) summary1.get("averageUtilization");
        BigDecimal avgUtil2 = (BigDecimal) summary2.get("averageUtilization");
        BigDecimal utilDiff = avgUtil1.subtract(avgUtil2);

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("creditDifference", creditDiff);
        comparison.put("debitDifference", debitDiff);
        comparison.put("utilizationDifference", utilDiff);
        comparison.put("creditPercentageDiff", calculatePercentageDiff(totalCredit1, totalCredit2));
        comparison.put("debitPercentageDiff", calculatePercentageDiff(totalDebit1, totalDebit2));
        comparison.put("utilizationPercentageDiff", calculatePercentageDiff(avgUtil1, avgUtil2));

        analysis.put("comparison", comparison);

        return analysis;
    }

    // Helper methods
    private AccountStatementDTO mapToDTO(AccountStatement statement) {
        return AccountStatementDTO.builder()
                .id(statement.getId())
                .month(statement.getMonth())
                .totalTurnoverCredit(statement.getTotalTurnoverCredit())
                .totalTurnoverDebit(statement.getTotalTurnoverDebit())
                .numberOfCreditEntries(statement.getNumberOfCreditEntries())
                .monthlyCreditAverage(statement.getMonthlyCreditAverage())
                .utilizationPercentage(statement.getUtilizationPercentage())
                .headerId(statement.getHeader() != null ? statement.getHeader().getId() : null)
                .build();
    }

    private void validateStatement(AccountStatementDTO dto) {
        if (dto.getTotalTurnoverCredit() != null && dto.getTotalTurnoverCredit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Total turnover credit cannot be negative");
        }
        if (dto.getTotalTurnoverDebit() != null && dto.getTotalTurnoverDebit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Total turnover debit cannot be negative");
        }
        if (dto.getNumberOfCreditEntries() != null && dto.getNumberOfCreditEntries() < 0) {
            throw new BadRequestException("Number of credit entries cannot be negative");
        }
    }

    private BigDecimal calculatePercentageDiff(BigDecimal value1, BigDecimal value2) {
        if (value2 == null || value2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return value1.subtract(value2)
                .multiply(BigDecimal.valueOf(100))
                .divide(value2, 2, RoundingMode.HALF_UP);
    }
}