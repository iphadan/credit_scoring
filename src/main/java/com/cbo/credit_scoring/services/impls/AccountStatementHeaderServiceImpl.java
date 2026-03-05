package com.cbo.credit_scoring.services.impls;

import com.cbo.credit_scoring.dtos.AccountStatementHeaderDTO;
import com.cbo.credit_scoring.dtos.AccountStatementDTO;
import com.cbo.credit_scoring.dtos.AccountStatementSummaryDTO;
import com.cbo.credit_scoring.models.AccountStatementHeader;
import com.cbo.credit_scoring.models.AccountStatement;
import com.cbo.credit_scoring.repositories.AccountStatementHeaderRepository;
import com.cbo.credit_scoring.repositories.AccountStatementRepository;
import com.cbo.credit_scoring.services.AccountStatementHeaderService;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountStatementHeaderServiceImpl implements AccountStatementHeaderService {

    private final AccountStatementHeaderRepository headerRepository;
    private final AccountStatementRepository statementRepository;
    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("80");

    @Override
    public AccountStatementHeaderDTO createHeader(AccountStatementHeaderDTO headerDTO) {
        log.info("Creating new account statement header for account holder: {}", headerDTO.getAccountHolder());

        // Validate required fields
        validateHeader(headerDTO);

        // Generate caseId if not provided
        if (headerDTO.getCaseId() == null || headerDTO.getCaseId().trim().isEmpty()) {
            headerDTO.setCaseId(generateCaseId(headerDTO));
        }

        // Check for duplicate caseId
        if (headerRepository.existsByCaseId(headerDTO.getCaseId())) {
            throw new DuplicateResourceException("Header with caseId " + headerDTO.getCaseId() + " already exists");
        }

        // Check for duplicate account number
        if (headerDTO.getAccountNumber() != null && !headerDTO.getAccountNumber().trim().isEmpty()) {
            if (headerRepository.existsByAccountNumber(headerDTO.getAccountNumber())) {
                throw new DuplicateResourceException("Header with account number " + headerDTO.getAccountNumber() + " already exists");
            }
        }

        // Set default status if not provided
        if (headerDTO.getStatus() == null || headerDTO.getStatus().trim().isEmpty()) {
            headerDTO.setStatus("ACTIVE");
        }

        // Convert DTO to Entity
        AccountStatementHeader header = mapToEntity(headerDTO);

        // Save header
        AccountStatementHeader savedHeader = headerRepository.save(header);
        log.info("Account statement header created successfully with ID: {}, CaseId: {}", savedHeader.getId(), savedHeader.getCaseId());

        return mapToDTO(savedHeader);
    }

    @Override
    public AccountStatementHeaderDTO updateHeader(Long id, AccountStatementHeaderDTO headerDTO) {
        log.info("Updating account statement header with ID: {}", id);

        // Find existing header
        AccountStatementHeader existingHeader = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + id));

        // Validate update data
        validateHeader(headerDTO);

        // Check account number uniqueness if changed
        if (headerDTO.getAccountNumber() != null && !headerDTO.getAccountNumber().equals(existingHeader.getAccountNumber())) {
            if (headerRepository.existsByAccountNumber(headerDTO.getAccountNumber())) {
                throw new DuplicateResourceException("Header with account number " + headerDTO.getAccountNumber() + " already exists");
            }
        }

        // Update fields
        updateHeaderFields(existingHeader, headerDTO);

        // Save updated header
        AccountStatementHeader updatedHeader = headerRepository.save(existingHeader);
        log.info("Account statement header updated successfully with ID: {}", updatedHeader.getId());

        return mapToDTO(updatedHeader);
    }

    @Override
    public AccountStatementHeaderDTO getHeaderById(Long id) {
        log.info("Fetching account statement header with ID: {}", id);

        AccountStatementHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + id));

        return mapToDTO(header);
    }

    @Override
    public AccountStatementHeaderDTO getHeaderByCaseId(String caseId) {
        log.info("Fetching account statement header with CaseId: {}", caseId);

        AccountStatementHeader header = headerRepository.findByCaseId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with CaseId: " + caseId));

        return mapToDTO(header);
    }

    @Override
    public Page<AccountStatementHeaderDTO> getAllHeaders(Pageable pageable) {
        log.info("Fetching all account statement headers with pagination");

        return headerRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public void deleteHeader(Long id) {
        log.info("Deleting account statement header with ID: {}", id);

        AccountStatementHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + id));

        headerRepository.delete(header);
        log.info("Account statement header deleted successfully with ID: {}", id);
    }

    @Override
    public List<AccountStatementHeaderDTO> searchByAccountHolder(String accountHolder) {
        log.info("Searching headers by account holder: {}", accountHolder);

        return headerRepository.findByAccountHolderContainingIgnoreCase(accountHolder)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountStatementHeaderDTO getHeaderByAccountNumber(String accountNumber) {
        log.info("Fetching header by account number: {}", accountNumber);

        AccountStatementHeader header = headerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with account number: " + accountNumber));

        return mapToDTO(header);
    }

    @Override
    public List<AccountStatementHeaderDTO> getHeadersByIndustry(String industry) {
        log.info("Fetching headers by industry: {}", industry);

        return headerRepository.findByIndustry(industry)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountStatementHeaderDTO> getHeadersByFacilityType(String facilityType) {
        log.info("Fetching headers by facility type: {}", facilityType);

        return headerRepository.findByFacilityType(facilityType)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountStatementHeaderDTO> getHeadersByStatus(String status) {
        log.info("Fetching headers by status: {}", status);

        return headerRepository.findByStatusIgnoreCase(status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountStatementHeaderDTO> getHeadersByApprovedDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching headers by approved date between: {} and {}", startDate, endDate);

        return headerRepository.findByApprovedDateBetween(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountStatementHeaderDTO> getHeadersByDateAccountOpenedRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching headers by date account opened between: {} and {}", startDate, endDate);

        return headerRepository.findByDateAccountOpenedBetween(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountStatementHeaderDTO> getHeadersByReportDate(LocalDate reportDate) {
        log.info("Fetching headers by report date: {}", reportDate);

        return headerRepository.findByReportDate(reportDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountStatementHeaderDTO> searchHeaders(String searchTerm) {
        log.info("Searching headers by any field with term: {}", searchTerm);

        return headerRepository.searchByAnyField(searchTerm)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountStatementHeaderDTO addStatementRecord(Long headerId, AccountStatementDTO statementDTO) {
        log.info("Adding statement record to header ID: {}", headerId);

        AccountStatementHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        // Validate statement data
        validateStatement(statementDTO);

        // Check for duplicate month
        if (statementDTO.getMonth() != null) {
            if (statementRepository.existsByHeaderIdAndMonth(headerId, statementDTO.getMonth())) {
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

        // Add to header using helper method
        header.addStatementRecord(statement);

        // Save header (cascade will save statement)
        AccountStatementHeader updatedHeader = headerRepository.save(header);
        log.info("Statement record added successfully to header ID: {}", headerId);

        return mapToDTO(updatedHeader);
    }

    @Override
    @Transactional
    public AccountStatementHeaderDTO updateStatementRecord(Long headerId, Long statementId, AccountStatementDTO statementDTO) {
        log.info("Updating statement record ID: {} in header ID: {}", statementId, headerId);

        AccountStatementHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        AccountStatement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement record not found with ID: " + statementId));

        // Verify statement belongs to header
        if (!statement.getHeader().getId().equals(headerId)) {
            throw new BadRequestException("Statement record does not belong to this header");
        }

        // Validate statement data
        validateStatement(statementDTO);

        // Check for duplicate month if month is being changed
        if (statementDTO.getMonth() != null && !statementDTO.getMonth().equals(statement.getMonth())) {
            if (statementRepository.existsByHeaderIdAndMonth(headerId, statementDTO.getMonth())) {
                throw new DuplicateResourceException("Statement record for month " + statementDTO.getMonth() + " already exists");
            }
        }

        // Update statement fields
        if (statementDTO.getMonth() != null) statement.setMonth(statementDTO.getMonth());
        if (statementDTO.getTotalTurnoverCredit() != null) statement.setTotalTurnoverCredit(statementDTO.getTotalTurnoverCredit());
        if (statementDTO.getTotalTurnoverDebit() != null) statement.setTotalTurnoverDebit(statementDTO.getTotalTurnoverDebit());
        if (statementDTO.getNumberOfCreditEntries() != null) statement.setNumberOfCreditEntries(statementDTO.getNumberOfCreditEntries());

        // Recalculate derived fields
        statement.calculateMonthlyCreditAverage();
        statement.calculateUtilizationPercentage(header.getSanctionLimit());

        // Save statement
        statementRepository.save(statement);
        log.info("Statement record updated successfully");

        return mapToDTO(header);
    }

    @Override
    @Transactional
    public AccountStatementHeaderDTO removeStatementRecord(Long headerId, Long statementId) {
        log.info("Removing statement record ID: {} from header ID: {}", statementId, headerId);

        AccountStatementHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        AccountStatement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement record not found with ID: " + statementId));

        // Verify statement belongs to header
        if (!statement.getHeader().getId().equals(headerId)) {
            throw new BadRequestException("Statement record does not belong to this header");
        }

        // Remove using helper method
        header.removeStatementRecord(statement);

        // Save header (cascade will remove statement)
        AccountStatementHeader updatedHeader = headerRepository.save(header);
        log.info("Statement record removed successfully from header ID: {}", headerId);

        return mapToDTO(updatedHeader);
    }

    @Override
    public AccountStatementSummaryDTO getHeaderSummary(Long headerId) {
        log.info("Generating summary for header ID: {}", headerId);

        AccountStatementHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        List<AccountStatement> statements = statementRepository.findByHeaderIdOrderByMonthAsc(headerId);

        if (statements.isEmpty()) {
            return buildEmptySummary(header);
        }

        BigDecimal totalCredit = statementRepository.sumTotalCreditByHeaderId(headerId);
        BigDecimal totalDebit = statementRepository.sumTotalDebitByHeaderId(headerId);
        BigDecimal avgMonthlyCredit = statementRepository.averageMonthlyCreditByHeaderId(headerId);
        BigDecimal avgUtilization = statementRepository.averageUtilizationByHeaderId(headerId);
        BigDecimal maxUtilization = statementRepository.maxUtilizationByHeaderId(headerId);
        BigDecimal minUtilization = statementRepository.minUtilizationByHeaderId(headerId);
        Integer totalCreditEntries = statementRepository.sumNumberOfCreditEntriesByHeaderId(headerId);

        long activeMonths = statementRepository.countActiveMonthsByHeaderId(headerId);

        Optional<AccountStatement> highestUtilMonth = statementRepository.findMonthWithHighestUtilization(headerId);
        Optional<AccountStatement> lowestUtilMonth = statementRepository.findMonthWithLowestUtilization(headerId);
        Optional<AccountStatement> latest = statementRepository.findLatestByHeaderId(headerId);

        BigDecimal avgMonthlyDebit = totalDebit.divide(BigDecimal.valueOf(statements.size()), 2, RoundingMode.HALF_UP);

        Map<YearMonth, BigDecimal> utilizationTrend = statements.stream()
                .filter(s -> s.getMonth() != null && s.getUtilizationPercentage() != null)
                .collect(Collectors.toMap(
                        AccountStatement::getMonth,
                        AccountStatement::getUtilizationPercentage,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));

        Map<YearMonth, BigDecimal> creditTrend = statements.stream()
                .filter(s -> s.getMonth() != null && s.getTotalTurnoverCredit() != null)
                .collect(Collectors.toMap(
                        AccountStatement::getMonth,
                        AccountStatement::getTotalTurnoverCredit,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));

        Map<YearMonth, BigDecimal> debitTrend = statements.stream()
                .filter(s -> s.getMonth() != null && s.getTotalTurnoverDebit() != null)
                .collect(Collectors.toMap(
                        AccountStatement::getMonth,
                        AccountStatement::getTotalTurnoverDebit,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));

        return AccountStatementSummaryDTO.builder()
                .headerId(headerId)
                .caseId(header.getCaseId())
                .accountHolder(header.getAccountHolder())
                .accountNumber(header.getAccountNumber())
                .sanctionLimit(header.getSanctionLimit())
                .totalCredit(totalCredit)
                .totalDebit(totalDebit)
                .netTurnover(totalCredit.subtract(totalDebit))
                .averageMonthlyCredit(avgMonthlyCredit)
                .averageMonthlyDebit(avgMonthlyDebit)
                .averageUtilization(avgUtilization)
                .maxUtilization(maxUtilization)
                .minUtilization(minUtilization)
                .totalMonths(statements.size())
                .activeMonths((int) activeMonths)
                .totalCreditEntries(totalCreditEntries)
                .lastTransactionMonth(latest.map(AccountStatement::getMonth).orElse(null))
                .highestUtilizationMonth(highestUtilMonth.map(AccountStatement::getMonth).orElse(null))
                .lowestUtilizationMonth(lowestUtilMonth.map(AccountStatement::getMonth).orElse(null))
                .dateAccountOpened(header.getDateAccountOpened())
                .reportDate(header.getReportDate())
                .status(header.getStatus())
                .monthlyUtilizationTrend(utilizationTrend)
                .monthlyCreditTrend(creditTrend)
                .monthlyDebitTrend(debitTrend)
                .build();
    }

    @Override
    public Map<String, Object> getHeaderStatistics(Long headerId) {
        log.info("Generating statistics for header ID: {}", headerId);

        AccountStatementHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        BigDecimal totalCredit = statementRepository.sumTotalCreditByHeaderId(headerId);
        BigDecimal totalDebit = statementRepository.sumTotalDebitByHeaderId(headerId);
        BigDecimal avgUtilization = statementRepository.averageUtilizationByHeaderId(headerId);
        BigDecimal maxUtilization = statementRepository.maxUtilizationByHeaderId(headerId);
        BigDecimal minUtilization = statementRepository.minUtilizationByHeaderId(headerId);
        Integer totalCreditEntries = statementRepository.sumNumberOfCreditEntriesByHeaderId(headerId);
        long recordCount = statementRepository.countByHeaderId(headerId);
        long monthsExceedingThreshold = statementRepository.countMonthsExceedingThreshold(headerId, DEFAULT_THRESHOLD);

        Optional<AccountStatement> latest = statementRepository.findLatestByHeaderId(headerId);
        Optional<AccountStatement> oldest = statementRepository.findOldestByHeaderId(headerId);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("headerId", headerId);
        statistics.put("caseId", header.getCaseId());
        statistics.put("accountHolder", header.getAccountHolder());
        statistics.put("totalCredit", totalCredit);
        statistics.put("totalDebit", totalDebit);
        statistics.put("netTurnover", totalCredit.subtract(totalDebit));
        statistics.put("averageUtilization", avgUtilization);
        statistics.put("maxUtilization", maxUtilization);
        statistics.put("minUtilization", minUtilization);
        statistics.put("totalCreditEntries", totalCreditEntries);
        statistics.put("totalMonths", recordCount);
        statistics.put("monthsExceeding80Percent", monthsExceedingThreshold);
        statistics.put("sanctionLimit", header.getSanctionLimit());
        statistics.put("latestMonth", latest.map(AccountStatement::getMonth).orElse(null));
        statistics.put("oldestMonth", oldest.map(AccountStatement::getMonth).orElse(null));
        statistics.put("accountAgeInMonths", calculateAccountAgeInMonths(header));
        statistics.put("utilizationVsLimit", calculateUtilizationVsLimit(maxUtilization, header.getSanctionLimit()));

        return statistics;
    }

    @Override
    public List<AccountStatementSummaryDTO> getAllHeadersSummary() {
        log.info("Generating summary for all headers");

        return headerRepository.findAll().stream()
                .map(header -> getHeaderSummary(header.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateTotalCredit(Long headerId) {
        return statementRepository.sumTotalCreditByHeaderId(headerId);
    }

    @Override
    public BigDecimal calculateTotalDebit(Long headerId) {
        return statementRepository.sumTotalDebitByHeaderId(headerId);
    }

    @Override
    public BigDecimal calculateNetTurnover(Long headerId) {
        BigDecimal totalCredit = calculateTotalCredit(headerId);
        BigDecimal totalDebit = calculateTotalDebit(headerId);
        return totalCredit.subtract(totalDebit);
    }

    @Override
    public BigDecimal calculateAverageUtilization(Long headerId) {
        return statementRepository.averageUtilizationByHeaderId(headerId);
    }

    @Override
    public BigDecimal calculateMaxUtilization(Long headerId) {
        return statementRepository.maxUtilizationByHeaderId(headerId);
    }

    @Override
    public BigDecimal calculateMinUtilization(Long headerId) {
        return statementRepository.minUtilizationByHeaderId(headerId);
    }

    @Override
    public Map<YearMonth, BigDecimal> getUtilizationTrend(Long headerId) {
        log.info("Generating utilization trend for header ID: {}", headerId);

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
    public Map<YearMonth, BigDecimal> getCreditTrend(Long headerId) {
        log.info("Generating credit trend for header ID: {}", headerId);

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
    public Map<YearMonth, BigDecimal> getDebitTrend(Long headerId) {
        log.info("Generating debit trend for header ID: {}", headerId);

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
    public List<AccountStatementDTO> getMonthsExceedingThreshold(Long headerId, BigDecimal threshold) {
        log.info("Fetching months exceeding threshold {} for header ID: {}", threshold, headerId);

        return statementRepository.findByHeaderId(headerId).stream()
                .filter(s -> s.getUtilizationPercentage() != null &&
                        s.getUtilizationPercentage().compareTo(threshold) > 0)
                .map(this::mapStatementToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    private AccountStatementHeader mapToEntity(AccountStatementHeaderDTO dto) {
        AccountStatementHeader header = new AccountStatementHeader();
        header.setCaseId(dto.getCaseId());
        header.setAccountHolder(dto.getAccountHolder());
        header.setAccountNumber(dto.getAccountNumber());
        header.setIndustry(dto.getIndustry());
        header.setSanctionLimit(dto.getSanctionLimit());
        header.setApprovedDate(dto.getApprovedDate());
        header.setFacilityType(dto.getFacilityType());
        header.setDateAccountOpened(dto.getDateAccountOpened());
        header.setReportDate(dto.getReportDate());
        header.setStatus(dto.getStatus());
        return header;
    }

    private AccountStatementHeaderDTO mapToDTO(AccountStatementHeader header) {
        AccountStatementHeaderDTO dto = AccountStatementHeaderDTO.builder()
                .id(header.getId())
                .caseId(header.getCaseId())
                .accountHolder(header.getAccountHolder())
                .accountNumber(header.getAccountNumber())
                .industry(header.getIndustry())
                .sanctionLimit(header.getSanctionLimit())
                .approvedDate(header.getApprovedDate())
                .facilityType(header.getFacilityType())
                .dateAccountOpened(header.getDateAccountOpened())
                .reportDate(header.getReportDate())
                .status(header.getStatus())
                .build();

        // Map statement records if they exist
        if (header.getStatementRecords() != null && !header.getStatementRecords().isEmpty()) {
            List<AccountStatementHeaderDTO.AccountStatementDTO> statementDTOs =
                    header.getStatementRecords().stream()
                            .map(this::mapStatementToNestedDTO)
                            .collect(Collectors.toList());
            dto.setStatementRecords(statementDTOs);
        }

        return dto;
    }

    private AccountStatementHeaderDTO.AccountStatementDTO mapStatementToNestedDTO(AccountStatement statement) {
        return AccountStatementHeaderDTO.AccountStatementDTO.builder()
                .id(statement.getId())
                .caseId(statement.getHeader().getCaseId())
                .month(statement.getMonth())
                .totalTurnoverCredit(statement.getTotalTurnoverCredit())
                .totalTurnoverDebit(statement.getTotalTurnoverDebit())
                .numberOfCreditEntries(statement.getNumberOfCreditEntries())
                .monthlyCreditAverage(statement.getMonthlyCreditAverage())
                .utilizationPercentage(statement.getUtilizationPercentage())
                .build();
    }

    private AccountStatementDTO mapStatementToDTO(AccountStatement statement) {
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

    private void updateHeaderFields(AccountStatementHeader existing, AccountStatementHeaderDTO dto) {
        if (dto.getAccountHolder() != null) existing.setAccountHolder(dto.getAccountHolder());
        if (dto.getAccountNumber() != null) existing.setAccountNumber(dto.getAccountNumber());
        if (dto.getIndustry() != null) existing.setIndustry(dto.getIndustry());
        if (dto.getSanctionLimit() != null) existing.setSanctionLimit(dto.getSanctionLimit());
        if (dto.getApprovedDate() != null) existing.setApprovedDate(dto.getApprovedDate());
        if (dto.getFacilityType() != null) existing.setFacilityType(dto.getFacilityType());
        if (dto.getDateAccountOpened() != null) existing.setDateAccountOpened(dto.getDateAccountOpened());
        if (dto.getReportDate() != null) existing.setReportDate(dto.getReportDate());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
    }

    private void validateHeader(AccountStatementHeaderDTO dto) {
        if (dto.getAccountHolder() == null || dto.getAccountHolder().trim().isEmpty()) {
            throw new BadRequestException("Account holder name is required");
        }
        if (dto.getAccountNumber() == null || dto.getAccountNumber().trim().isEmpty()) {
            throw new BadRequestException("Account number is required");
        }
        if (dto.getSanctionLimit() == null) {
            throw new BadRequestException("Sanction limit is required");
        }
        if (dto.getSanctionLimit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Sanction limit cannot be negative");
        }
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

    private String generateCaseId(AccountStatementHeaderDTO dto) {
        String prefix = dto.getAccountHolder().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (prefix.length() > 5) {
            prefix = prefix.substring(0, 5);
        }
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        return "AST-" + prefix + "-" + timestamp;
    }

    private BigDecimal calculateUtilizationVsLimit(BigDecimal utilization, BigDecimal sanctionLimit) {
        if (sanctionLimit == null || sanctionLimit.compareTo(BigDecimal.ZERO) == 0 || utilization == null) {
            return BigDecimal.ZERO;
        }
        return utilization.multiply(BigDecimal.valueOf(100))
                .divide(sanctionLimit, 2, RoundingMode.HALF_UP);
    }

    private Integer calculateAccountAgeInMonths(AccountStatementHeader header) {
        if (header.getDateAccountOpened() == null) {
            return 0;
        }
        LocalDate now = LocalDate.now();
        return (int) java.time.temporal.ChronoUnit.MONTHS.between(
                header.getDateAccountOpened().withDayOfMonth(1),
                now.withDayOfMonth(1));
    }

    private AccountStatementSummaryDTO buildEmptySummary(AccountStatementHeader header) {
        return AccountStatementSummaryDTO.builder()
                .headerId(header.getId())
                .caseId(header.getCaseId())
                .accountHolder(header.getAccountHolder())
                .accountNumber(header.getAccountNumber())
                .sanctionLimit(header.getSanctionLimit())
                .totalCredit(BigDecimal.ZERO)
                .totalDebit(BigDecimal.ZERO)
                .netTurnover(BigDecimal.ZERO)
                .averageMonthlyCredit(BigDecimal.ZERO)
                .averageMonthlyDebit(BigDecimal.ZERO)
                .averageUtilization(BigDecimal.ZERO)
                .maxUtilization(BigDecimal.ZERO)
                .minUtilization(BigDecimal.ZERO)
                .totalMonths(0)
                .activeMonths(0)
                .totalCreditEntries(0)
                .lastTransactionMonth(null)
                .highestUtilizationMonth(null)
                .lowestUtilizationMonth(null)
                .dateAccountOpened(header.getDateAccountOpened())
                .reportDate(header.getReportDate())
                .status(header.getStatus())
                .monthlyUtilizationTrend(new TreeMap<>())
                .monthlyCreditTrend(new TreeMap<>())
                .monthlyDebitTrend(new TreeMap<>())
                .build();
    }
}