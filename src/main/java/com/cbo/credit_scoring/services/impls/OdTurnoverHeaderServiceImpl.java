package com.cbo.credit_scoring.services.impls;

import com.cbo.credit_scoring.dtos.OdTurnoverHeaderDTO;
import com.cbo.credit_scoring.dtos.OdTurnoverDTO;
import com.cbo.credit_scoring.dtos.OdTurnoverSummaryDTO;
import com.cbo.credit_scoring.models.OdTurnoverHeader;
import com.cbo.credit_scoring.models.OdTurnover;
import com.cbo.credit_scoring.repositories.OdTurnoverHeaderRepository;
import com.cbo.credit_scoring.repositories.OdTurnoverRepository;
import com.cbo.credit_scoring.services.OdTurnoverHeaderService;
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
public class OdTurnoverHeaderServiceImpl implements OdTurnoverHeaderService {

    private final OdTurnoverHeaderRepository headerRepository;
    private final OdTurnoverRepository turnoverRepository;


    @Override
    @Transactional
    public OdTurnoverHeaderDTO createHeader(OdTurnoverHeaderDTO headerDTO) {
        log.info("Creating new OD turnover header for account holder: {}", headerDTO.getAccountHolder());

        // Validate required fields
        validateHeader(headerDTO);

        // Generate caseId if not provided
        if (headerDTO.getCaseId() == null || headerDTO.getCaseId().trim().isEmpty()) {
            throw new ResourceNotFoundException("Case ID Must be provided");
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

        // Convert DTO to Entity (header only)
        OdTurnoverHeader header = mapToEntity(headerDTO);

        // ===== IMPORTANT: Handle OD Turnover Records =====
        if (headerDTO.getTurnoverRecords() != null && !headerDTO.getTurnoverRecords().isEmpty()) {
            log.info("Processing {} OD turnover records", headerDTO.getTurnoverRecords().size());

            // Validate all turnover records first
            validateTurnoverRecords(headerDTO.getTurnoverRecords());

            // Check for duplicate months within the request
            checkForDuplicateMonths(headerDTO.getTurnoverRecords());

            // Create and add each turnover record to the header
            for (OdTurnoverHeaderDTO.OdTurnoverDTO turnoverDTO : headerDTO.getTurnoverRecords()) {

                // Create turnover entity
                OdTurnover turnover = new OdTurnover();
                turnover.setMonth(turnoverDTO.getMonth());
                turnover.setTotalTurnoverCredit(turnoverDTO.getTotalTurnoverCredit() != null ?
                        turnoverDTO.getTotalTurnoverCredit() : BigDecimal.ZERO);
                turnover.setTotalTurnoverDebit(turnoverDTO.getTotalTurnoverDebit() != null ?
                        turnoverDTO.getTotalTurnoverDebit() : BigDecimal.ZERO);
                turnover.setNumberOfCreditEntries(turnoverDTO.getNumberOfCreditEntries() != null ?
                        turnoverDTO.getNumberOfCreditEntries() : 0);

                // Monthly credit average and utilization percentage will be calculated
                // automatically by the @PrePersist and @PreUpdate methods in OdTurnover entity

                // Use the helper method to establish bidirectional relationship
                header.addTurnoverRecord(turnover);

                log.debug("Added OD turnover record for month: {}", turnoverDTO.getMonth());
            }
        }

        // Save header - THIS WILL ALSO SAVE ALL TURNOVER RECORDS due to CascadeType.ALL
        OdTurnoverHeader savedHeader = headerRepository.save(header);
        log.info("OD header created successfully with ID: {}, CaseId: {}, with {} turnover records",
                savedHeader.getId(), savedHeader.getCaseId(),
                savedHeader.getTurnoverRecords() != null ? savedHeader.getTurnoverRecords().size() : 0);

        return mapToDTO(savedHeader);
    }

    /**
     * Validate all turnover records
     */
    private void validateTurnoverRecords(List<OdTurnoverHeaderDTO.OdTurnoverDTO> turnoverRecords) {
        for (OdTurnoverHeaderDTO.OdTurnoverDTO dto : turnoverRecords) {
            if (dto.getMonth() == null) {
                throw new BadRequestException("Month is required for each turnover record");
            }
            if (dto.getTotalTurnoverCredit() != null && dto.getTotalTurnoverCredit().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Total turnover credit cannot be negative for month: " + dto.getMonth());
            }
            if (dto.getTotalTurnoverDebit() != null && dto.getTotalTurnoverDebit().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Total turnover debit cannot be negative for month: " + dto.getMonth());
            }
            if (dto.getNumberOfCreditEntries() != null && dto.getNumberOfCreditEntries() < 0) {
                throw new BadRequestException("Number of credit entries cannot be negative for month: " + dto.getMonth());
            }
        }
    }

    /**
     * Check for duplicate months within the same request
     */
    private void checkForDuplicateMonths(List<OdTurnoverHeaderDTO.OdTurnoverDTO> turnoverRecords) {
        Set<YearMonth> months = new HashSet<>();
        for (OdTurnoverHeaderDTO.OdTurnoverDTO dto : turnoverRecords) {
            if (!months.add(dto.getMonth())) {
                throw new BadRequestException("Duplicate month " + dto.getMonth() + " found in request");
            }
        }
    }
    @Override
    public OdTurnoverHeaderDTO updateHeader(Long id, OdTurnoverHeaderDTO headerDTO) {
        log.info("Updating OD turnover header with ID: {}", id);

        // Find existing header
        OdTurnoverHeader existingHeader = headerRepository.findById(id)
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
        OdTurnoverHeader updatedHeader = headerRepository.save(existingHeader);
        log.info("OD header updated successfully with ID: {}", updatedHeader.getId());

        return mapToDTO(updatedHeader);
    }

    @Override
    public OdTurnoverHeaderDTO getHeaderById(Long id) {
        log.info("Fetching OD turnover header with ID: {}", id);

        OdTurnoverHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + id));

        return mapToDTO(header);
    }

    @Override
    public OdTurnoverHeaderDTO getHeaderByCaseId(String caseId) {
        log.info("Fetching OD turnover header with CaseId: {}", caseId);

        OdTurnoverHeader header = headerRepository.findByCaseId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with CaseId: " + caseId));

        return mapToDTO(header);
    }

    @Override
    public Page<OdTurnoverHeaderDTO> getAllHeaders(Pageable pageable) {
        log.info("Fetching all OD turnover headers with pagination");

        return headerRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public void deleteHeader(Long id) {
        log.info("Deleting OD turnover header with ID: {}", id);

        OdTurnoverHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + id));

        headerRepository.delete(header);
        log.info("OD header deleted successfully with ID: {}", id);
    }

    @Override
    public List<OdTurnoverHeaderDTO> searchByAccountHolder(String accountHolder) {
        log.info("Searching headers by account holder: {}", accountHolder);

        return headerRepository.findByAccountHolderContainingIgnoreCase(accountHolder)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OdTurnoverHeaderDTO getHeaderByAccountNumber(String accountNumber) {
        log.info("Fetching header by account number: {}", accountNumber);

        OdTurnoverHeader header = headerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with account number: " + accountNumber));

        return mapToDTO(header);
    }

    @Override
    public List<OdTurnoverHeaderDTO> getHeadersByIndustry(String industry) {
        log.info("Fetching headers by industry: {}", industry);

        return headerRepository.findByIndustry(industry)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdTurnoverHeaderDTO> getHeadersByFacilityType(String facilityType) {
        log.info("Fetching headers by facility type: {}", facilityType);

        return headerRepository.findByFacilityType(facilityType)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdTurnoverHeaderDTO> getHeadersByStatus(String status) {
        log.info("Fetching headers by status: {}", status);

        return headerRepository.findByStatusIgnoreCase(status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdTurnoverHeaderDTO> getHeadersByApprovedDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching headers by approved date between: {} and {}", startDate, endDate);

        return headerRepository.findByApprovedDateBetween(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdTurnoverHeaderDTO> getHeadersByReportDate(LocalDate reportDate) {
        log.info("Fetching headers by report date: {}", reportDate);

        return headerRepository.findByReportDate(reportDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdTurnoverHeaderDTO> searchHeaders(String searchTerm) {
        log.info("Searching headers by any field with term: {}", searchTerm);

        return headerRepository.searchByAnyField(searchTerm)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OdTurnoverHeaderDTO addTurnoverRecord(Long headerId, OdTurnoverDTO turnoverDTO) {
        log.info("Adding turnover record to header ID: {}", headerId);

        OdTurnoverHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        // Validate turnover data
        validateTurnover(turnoverDTO);

        // Check for duplicate month
        if (turnoverDTO.getMonth() != null) {
            if (turnoverRepository.existsByHeaderIdAndMonth(headerId, turnoverDTO.getMonth())) {
                throw new DuplicateResourceException("Turnover record for month " + turnoverDTO.getMonth() + " already exists");
            }
        }

        // Create new turnover record
        OdTurnover turnover = new OdTurnover();
        turnover.setMonth(turnoverDTO.getMonth());
        turnover.setTotalTurnoverCredit(turnoverDTO.getTotalTurnoverCredit() != null ?
                turnoverDTO.getTotalTurnoverCredit() : BigDecimal.ZERO);
        turnover.setTotalTurnoverDebit(turnoverDTO.getTotalTurnoverDebit() != null ?
                turnoverDTO.getTotalTurnoverDebit() : BigDecimal.ZERO);
        turnover.setNumberOfCreditEntries(turnoverDTO.getNumberOfCreditEntries() != null ?
                turnoverDTO.getNumberOfCreditEntries() : 0);

        // Calculate derived fields
        turnover.calculateMonthlyCreditAverage();
        turnover.calculateUtilizationPercentage(header.getSanctionLimit());

        // Add to header using helper method
        header.addTurnoverRecord(turnover);

        // Save header (cascade will save turnover)
        OdTurnoverHeader updatedHeader = headerRepository.save(header);
        log.info("Turnover record added successfully to header ID: {}", headerId);

        return mapToDTO(updatedHeader);
    }

    @Override
    @Transactional
    public OdTurnoverHeaderDTO updateTurnoverRecord(Long headerId, Long turnoverId, OdTurnoverDTO turnoverDTO) {
        log.info("Updating turnover record ID: {} in header ID: {}", turnoverId, headerId);

        OdTurnoverHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        OdTurnover turnover = turnoverRepository.findById(turnoverId)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + turnoverId));

        // Verify turnover belongs to header
        if (!turnover.getHeader().getId().equals(headerId)) {
            throw new BadRequestException("Turnover record does not belong to this header");
        }

        // Validate turnover data
        validateTurnover(turnoverDTO);

        // Check for duplicate month if month is being changed
        if (turnoverDTO.getMonth() != null && !turnoverDTO.getMonth().equals(turnover.getMonth())) {
            if (turnoverRepository.existsByHeaderIdAndMonth(headerId, turnoverDTO.getMonth())) {
                throw new DuplicateResourceException("Turnover record for month " + turnoverDTO.getMonth() + " already exists");
            }
        }

        // Update turnover fields
        if (turnoverDTO.getMonth() != null) turnover.setMonth(turnoverDTO.getMonth());
        if (turnoverDTO.getTotalTurnoverCredit() != null) turnover.setTotalTurnoverCredit(turnoverDTO.getTotalTurnoverCredit());
        if (turnoverDTO.getTotalTurnoverDebit() != null) turnover.setTotalTurnoverDebit(turnoverDTO.getTotalTurnoverDebit());
        if (turnoverDTO.getNumberOfCreditEntries() != null) turnover.setNumberOfCreditEntries(turnoverDTO.getNumberOfCreditEntries());

        // Recalculate derived fields
        turnover.calculateMonthlyCreditAverage();
        turnover.calculateUtilizationPercentage(header.getSanctionLimit());

        // Save turnover
        turnoverRepository.save(turnover);
        log.info("Turnover record updated successfully");

        return mapToDTO(header);
    }

    @Override
    @Transactional
    public OdTurnoverHeaderDTO removeTurnoverRecord(Long headerId, Long turnoverId) {
        log.info("Removing turnover record ID: {} from header ID: {}", turnoverId, headerId);

        OdTurnoverHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        OdTurnover turnover = turnoverRepository.findById(turnoverId)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + turnoverId));

        // Verify turnover belongs to header
        if (!turnover.getHeader().getId().equals(headerId)) {
            throw new BadRequestException("Turnover record does not belong to this header");
        }

        // Remove using helper method
        header.removeTurnoverRecord(turnover);

        // Save header (cascade will remove turnover)
        OdTurnoverHeader updatedHeader = headerRepository.save(header);
        log.info("Turnover record removed successfully from header ID: {}", headerId);

        return mapToDTO(updatedHeader);
    }

    @Override
    public OdTurnoverSummaryDTO getHeaderSummary(Long headerId) {
        log.info("Generating summary for header ID: {}", headerId);

        OdTurnoverHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        List<OdTurnover> turnovers = turnoverRepository.findByHeaderId(headerId);

        if (turnovers.isEmpty()) {
            return buildEmptySummary(header);
        }

        BigDecimal totalCredit = turnovers.stream()
                .map(OdTurnover::getTotalTurnoverCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebit = turnovers.stream()
                .map(OdTurnover::getTotalTurnoverDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgMonthlyCredit = turnovers.stream()
                .map(OdTurnover::getMonthlyCreditAverage)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(turnovers.size()), 2, RoundingMode.HALF_UP);

        BigDecimal avgMonthlyDebit = turnovers.stream()
                .map(OdTurnover::getTotalTurnoverDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(turnovers.size()), 2, RoundingMode.HALF_UP);

        BigDecimal avgUtilization = turnovers.stream()
                .map(OdTurnover::getUtilizationPercentage)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(turnovers.size()), 2, RoundingMode.HALF_UP);

        Integer activeMonths = (int) turnovers.stream()
                .filter(t -> t.getUtilizationPercentage() != null &&
                        t.getUtilizationPercentage().compareTo(BigDecimal.ZERO) > 0)
                .count();

        YearMonth lastMonth = turnovers.stream()
                .map(OdTurnover::getMonth)
                .max(YearMonth::compareTo)
                .orElse(null);

        Map<YearMonth, BigDecimal> utilizationTrend = turnovers.stream()
                .filter(t -> t.getMonth() != null && t.getUtilizationPercentage() != null)
                .collect(Collectors.toMap(
                        OdTurnover::getMonth,
                        OdTurnover::getUtilizationPercentage,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));

        return OdTurnoverSummaryDTO.builder()
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
                .totalMonths(turnovers.size())
                .activeMonths(activeMonths)
                .lastTransactionMonth(lastMonth)
                .monthlyUtilizationTrend(utilizationTrend)
                .status(header.getStatus())
                .build();
    }

    @Override
    public Map<String, Object> getHeaderStatistics(Long headerId) {
        log.info("Generating statistics for header ID: {}", headerId);

        OdTurnoverHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        BigDecimal totalCredit = turnoverRepository.sumTotalCreditByHeaderId(headerId);
        BigDecimal totalDebit = turnoverRepository.sumTotalDebitByHeaderId(headerId);
        BigDecimal avgUtilization = turnoverRepository.averageUtilizationByHeaderId(headerId);
        Integer totalCreditEntries = turnoverRepository.sumNumberOfCreditEntriesByHeaderId(headerId);
        long recordCount = turnoverRepository.countByHeaderId(headerId);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("headerId", headerId);
        statistics.put("caseId", header.getCaseId());
        statistics.put("accountHolder", header.getAccountHolder());
        statistics.put("totalCredit", totalCredit);
        statistics.put("totalDebit", totalDebit);
        statistics.put("netTurnover", totalCredit.subtract(totalDebit));
        statistics.put("averageUtilization", avgUtilization);
        statistics.put("totalCreditEntries", totalCreditEntries);
        statistics.put("recordCount", recordCount);
        statistics.put("sanctionLimit", header.getSanctionLimit());
        statistics.put("utilizationVsLimit", calculateUtilizationVsLimit(totalDebit, header.getSanctionLimit()));

        return statistics;
    }

    @Override
    public List<OdTurnoverSummaryDTO> getAllHeadersSummary() {
        log.info("Generating summary for all headers");

        return headerRepository.findAll().stream()
                .map(header -> getHeaderSummary(header.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateTotalCredit(Long headerId) {
        return turnoverRepository.sumTotalCreditByHeaderId(headerId);
    }

    @Override
    public BigDecimal calculateTotalDebit(Long headerId) {
        return turnoverRepository.sumTotalDebitByHeaderId(headerId);
    }

    @Override
    public BigDecimal calculateNetTurnover(Long headerId) {
        BigDecimal totalCredit = calculateTotalCredit(headerId);
        BigDecimal totalDebit = calculateTotalDebit(headerId);
        return totalCredit.subtract(totalDebit);
    }

    @Override
    public BigDecimal calculateAverageUtilization(Long headerId) {
        return turnoverRepository.averageUtilizationByHeaderId(headerId);
    }

    @Override
    public Map<YearMonth, BigDecimal> getUtilizationTrend(Long headerId) {
        log.info("Generating utilization trend for header ID: {}", headerId);

        List<OdTurnover> turnovers = turnoverRepository.findByHeaderId(headerId);

        return turnovers.stream()
                .filter(t -> t.getMonth() != null && t.getUtilizationPercentage() != null)
                .collect(Collectors.toMap(
                        OdTurnover::getMonth,
                        OdTurnover::getUtilizationPercentage,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));
    }

    // Helper methods
    private OdTurnoverHeader mapToEntity(OdTurnoverHeaderDTO dto) {
        OdTurnoverHeader header = new OdTurnoverHeader();
        header.setCaseId(dto.getCaseId());
        header.setAccountHolder(dto.getAccountHolder());
        header.setAccountNumber(dto.getAccountNumber());
        header.setIndustry(dto.getIndustry());
        header.setSanctionLimit(dto.getSanctionLimit());
        header.setApprovedDate(dto.getApprovedDate());
        header.setFacilityType(dto.getFacilityType());
        header.setReportDate(dto.getReportDate());
        header.setStatus(dto.getStatus());
        return header;
    }

    private OdTurnoverHeaderDTO mapToDTO(OdTurnoverHeader header) {
        OdTurnoverHeaderDTO dto = OdTurnoverHeaderDTO.builder()
                .id(header.getId())
                .caseId(header.getCaseId())
                .accountHolder(header.getAccountHolder())
                .accountNumber(header.getAccountNumber())
                .industry(header.getIndustry())
                .sanctionLimit(header.getSanctionLimit())
                .approvedDate(header.getApprovedDate())
                .facilityType(header.getFacilityType())
                .reportDate(header.getReportDate())
                .status(header.getStatus())
                .build();

        // Map turnover records if they exist
        if (header.getTurnoverRecords() != null && !header.getTurnoverRecords().isEmpty()) {
            List<OdTurnoverHeaderDTO.OdTurnoverDTO> turnoverDTOs =
                    header.getTurnoverRecords().stream()
                            .map(this::mapTurnoverToNestedDTO)
                            .collect(Collectors.toList());
            dto.setTurnoverRecords(turnoverDTOs);
        }

        return dto;
    }

    private OdTurnoverHeaderDTO.OdTurnoverDTO mapTurnoverToNestedDTO(OdTurnover turnover) {
        return OdTurnoverHeaderDTO.OdTurnoverDTO.builder()
                .id(turnover.getId())
                .caseId(turnover.getHeader().getCaseId())
                .month(turnover.getMonth())
                .totalTurnoverCredit(turnover.getTotalTurnoverCredit())
                .totalTurnoverDebit(turnover.getTotalTurnoverDebit())
                .numberOfCreditEntries(turnover.getNumberOfCreditEntries())
                .monthlyCreditAverage(turnover.getMonthlyCreditAverage())
                .utilizationPercentage(turnover.getUtilizationPercentage())
                .build();
    }

    private void updateHeaderFields(OdTurnoverHeader existing, OdTurnoverHeaderDTO dto) {
        if (dto.getAccountHolder() != null) existing.setAccountHolder(dto.getAccountHolder());
        if (dto.getAccountNumber() != null) existing.setAccountNumber(dto.getAccountNumber());
        if (dto.getIndustry() != null) existing.setIndustry(dto.getIndustry());
        if (dto.getSanctionLimit() != null) existing.setSanctionLimit(dto.getSanctionLimit());
        if (dto.getApprovedDate() != null) existing.setApprovedDate(dto.getApprovedDate());
        if (dto.getFacilityType() != null) existing.setFacilityType(dto.getFacilityType());
        if (dto.getReportDate() != null) existing.setReportDate(dto.getReportDate());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
    }

    private void validateHeader(OdTurnoverHeaderDTO dto) {
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

    private void validateTurnover(OdTurnoverDTO dto) {
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

    private String generateCaseId(OdTurnoverHeaderDTO dto) {
        String prefix = dto.getAccountHolder().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (prefix.length() > 5) {
            prefix = prefix.substring(0, 5);
        }
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        return "OD-" + prefix + "-" + timestamp;
    }

    private BigDecimal calculateUtilizationVsLimit(BigDecimal totalDebit, BigDecimal sanctionLimit) {
        if (sanctionLimit == null || sanctionLimit.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalDebit.multiply(BigDecimal.valueOf(100))
                .divide(sanctionLimit, 2, RoundingMode.HALF_UP);
    }

    private OdTurnoverSummaryDTO buildEmptySummary(OdTurnoverHeader header) {
        return OdTurnoverSummaryDTO.builder()
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
                .totalMonths(0)
                .activeMonths(0)
                .lastTransactionMonth(null)
                .monthlyUtilizationTrend(new TreeMap<>())
                .status(header.getStatus())
                .build();
    }
}