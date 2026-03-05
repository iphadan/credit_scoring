package com.cbo.credit_scoring.services.impl;

import com.cbo.credit_scoring.dtos.OdSwingHeaderDTO;
import com.cbo.credit_scoring.dtos.OdSwingDTO;
import com.cbo.credit_scoring.dtos.OdSwingSummaryDTO;
import com.cbo.credit_scoring.models.OdSwingHeader;
import com.cbo.credit_scoring.models.OdSwing;
import com.cbo.credit_scoring.repositories.OdSwingHeaderRepository;
import com.cbo.credit_scoring.repositories.OdSwingRepository;
import com.cbo.credit_scoring.services.OdSwingHeaderService;
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
public class OdSwingHeaderServiceImpl implements OdSwingHeaderService {

    private final OdSwingHeaderRepository headerRepository;
    private final OdSwingRepository swingRepository;
    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("80");

    @Override
    public OdSwingHeaderDTO createHeader(OdSwingHeaderDTO headerDTO) {
        log.info("Creating new OD swing header for account holder: {}", headerDTO.getAccountHolder());

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
        OdSwingHeader header = mapToEntity(headerDTO);

        // Save header
        OdSwingHeader savedHeader = headerRepository.save(header);
        log.info("OD swing header created successfully with ID: {}, CaseId: {}", savedHeader.getId(), savedHeader.getCaseId());

        return mapToDTO(savedHeader);
    }

    @Override
    public OdSwingHeaderDTO updateHeader(Long id, OdSwingHeaderDTO headerDTO) {
        log.info("Updating OD swing header with ID: {}", id);

        // Find existing header
        OdSwingHeader existingHeader = headerRepository.findById(id)
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
        OdSwingHeader updatedHeader = headerRepository.save(existingHeader);
        log.info("OD swing header updated successfully with ID: {}", updatedHeader.getId());

        return mapToDTO(updatedHeader);
    }

    @Override
    public OdSwingHeaderDTO getHeaderById(Long id) {
        log.info("Fetching OD swing header with ID: {}", id);

        OdSwingHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + id));

        return mapToDTO(header);
    }

    @Override
    public OdSwingHeaderDTO getHeaderByCaseId(String caseId) {
        log.info("Fetching OD swing header with CaseId: {}", caseId);

        OdSwingHeader header = headerRepository.findByCaseId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with CaseId: " + caseId));

        return mapToDTO(header);
    }

    @Override
    public Page<OdSwingHeaderDTO> getAllHeaders(Pageable pageable) {
        log.info("Fetching all OD swing headers with pagination");

        return headerRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public void deleteHeader(Long id) {
        log.info("Deleting OD swing header with ID: {}", id);

        OdSwingHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + id));

        headerRepository.delete(header);
        log.info("OD swing header deleted successfully with ID: {}", id);
    }

    @Override
    public List<OdSwingHeaderDTO> searchByAccountHolder(String accountHolder) {
        log.info("Searching headers by account holder: {}", accountHolder);

        return headerRepository.findByAccountHolderContainingIgnoreCase(accountHolder)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OdSwingHeaderDTO getHeaderByAccountNumber(String accountNumber) {
        log.info("Fetching header by account number: {}", accountNumber);

        OdSwingHeader header = headerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with account number: " + accountNumber));

        return mapToDTO(header);
    }

    @Override
    public List<OdSwingHeaderDTO> getHeadersByIndustry(String industry) {
        log.info("Fetching headers by industry: {}", industry);

        return headerRepository.findByIndustry(industry)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdSwingHeaderDTO> getHeadersByFacilityType(String facilityType) {
        log.info("Fetching headers by facility type: {}", facilityType);

        return headerRepository.findByFacilityType(facilityType)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdSwingHeaderDTO> getHeadersByStatus(String status) {
        log.info("Fetching headers by status: {}", status);

        return headerRepository.findByStatusIgnoreCase(status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdSwingHeaderDTO> getHeadersByApprovedDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching headers by approved date between: {} and {}", startDate, endDate);

        return headerRepository.findByApprovedDateBetween(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdSwingHeaderDTO> getHeadersByReportDate(LocalDate reportDate) {
        log.info("Fetching headers by report date: {}", reportDate);

        return headerRepository.findByReportDate(reportDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdSwingHeaderDTO> searchHeaders(String searchTerm) {
        log.info("Searching headers by any field with term: {}", searchTerm);

        return headerRepository.searchByAnyField(searchTerm)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OdSwingHeaderDTO addSwingRecord(Long headerId, OdSwingDTO swingDTO) {
        log.info("Adding swing record to header ID: {}", headerId);

        OdSwingHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        // Validate swing data
        validateSwing(swingDTO);

        // Check for duplicate month
        if (swingDTO.getMonth() != null) {
            if (swingRepository.existsByHeaderIdAndMonth(headerId, swingDTO.getMonth())) {
                throw new DuplicateResourceException("Swing record for month " + swingDTO.getMonth() + " already exists");
            }
        }

        // Validate date logic
        if (swingDTO.getDateHigh() != null && swingDTO.getDateLow() != null) {
            if (swingDTO.getDateHigh().isBefore(swingDTO.getDateLow())) {
                throw new BadRequestException("Date high cannot be before date low");
            }
        }

        // Create new swing record
        OdSwing swing = new OdSwing();
        swing.setMonth(swingDTO.getMonth());
        swing.setHighestUtilization(swingDTO.getHighestUtilization());
        swing.setDateHigh(swingDTO.getDateHigh());
        swing.setLowestUtilization(swingDTO.getLowestUtilization());
        swing.setDateLow(swingDTO.getDateLow());
        swing.setUtilizationPercentage(swingDTO.getUtilizationPercentage());

        // Add to header using helper method
        header.addSwingRecord(swing);

        // Save header (cascade will save swing)
        OdSwingHeader updatedHeader = headerRepository.save(header);
        log.info("Swing record added successfully to header ID: {}", headerId);

        return mapToDTO(updatedHeader);
    }

    @Override
    @Transactional
    public OdSwingHeaderDTO updateSwingRecord(Long headerId, Long swingId, OdSwingDTO swingDTO) {
        log.info("Updating swing record ID: {} in header ID: {}", swingId, headerId);

        OdSwingHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        OdSwing swing = swingRepository.findById(swingId)
                .orElseThrow(() -> new ResourceNotFoundException("Swing record not found with ID: " + swingId));

        // Verify swing belongs to header
        if (!swing.getHeader().getId().equals(headerId)) {
            throw new BadRequestException("Swing record does not belong to this header");
        }

        // Validate swing data
        validateSwing(swingDTO);

        // Validate date logic
        if (swingDTO.getDateHigh() != null && swingDTO.getDateLow() != null) {
            if (swingDTO.getDateHigh().isBefore(swingDTO.getDateLow())) {
                throw new BadRequestException("Date high cannot be before date low");
            }
        }

        // Check for duplicate month if month is being changed
        if (swingDTO.getMonth() != null && !swingDTO.getMonth().equals(swing.getMonth())) {
            if (swingRepository.existsByHeaderIdAndMonth(headerId, swingDTO.getMonth())) {
                throw new DuplicateResourceException("Swing record for month " + swingDTO.getMonth() + " already exists");
            }
        }

        // Update swing fields
        if (swingDTO.getMonth() != null) swing.setMonth(swingDTO.getMonth());
        if (swingDTO.getHighestUtilization() != null) swing.setHighestUtilization(swingDTO.getHighestUtilization());
        if (swingDTO.getDateHigh() != null) swing.setDateHigh(swingDTO.getDateHigh());
        if (swingDTO.getLowestUtilization() != null) swing.setLowestUtilization(swingDTO.getLowestUtilization());
        if (swingDTO.getDateLow() != null) swing.setDateLow(swingDTO.getDateLow());
        if (swingDTO.getUtilizationPercentage() != null) swing.setUtilizationPercentage(swingDTO.getUtilizationPercentage());

        // Save swing
        swingRepository.save(swing);
        log.info("Swing record updated successfully");

        return mapToDTO(header);
    }

    @Override
    @Transactional
    public OdSwingHeaderDTO removeSwingRecord(Long headerId, Long swingId) {
        log.info("Removing swing record ID: {} from header ID: {}", swingId, headerId);

        OdSwingHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        OdSwing swing = swingRepository.findById(swingId)
                .orElseThrow(() -> new ResourceNotFoundException("Swing record not found with ID: " + swingId));

        // Verify swing belongs to header
        if (!swing.getHeader().getId().equals(headerId)) {
            throw new BadRequestException("Swing record does not belong to this header");
        }

        // Remove using helper method
        header.removeSwingRecord(swing);

        // Save header (cascade will remove swing)
        OdSwingHeader updatedHeader = headerRepository.save(header);
        log.info("Swing record removed successfully from header ID: {}", headerId);

        return mapToDTO(updatedHeader);
    }

    @Override
    public OdSwingSummaryDTO getHeaderSummary(Long headerId) {
        log.info("Generating summary for header ID: {}", headerId);

        OdSwingHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        List<OdSwing> swings = swingRepository.findByHeaderIdOrderByMonth(headerId);

        if (swings.isEmpty()) {
            return buildEmptySummary(header);
        }

        BigDecimal avgHighest = swingRepository.averageHighestUtilizationByHeaderId(headerId);
        BigDecimal avgLowest = swingRepository.averageLowestUtilizationByHeaderId(headerId);
        BigDecimal avgUtilization = swingRepository.averageUtilizationByHeaderId(headerId);
        BigDecimal maxUtilization = swingRepository.maxUtilizationByHeaderId(headerId);
        BigDecimal minUtilization = swingRepository.minUtilizationByHeaderId(headerId);
        BigDecimal avgSwingRange = swingRepository.averageSwingRangeByHeaderId(headerId);
        BigDecimal maxSwingRange = swingRepository.maxSwingRangeByHeaderId(headerId);

        Optional<OdSwing> highestMonth = swingRepository.findMonthWithHighestUtilization(headerId);
        Optional<OdSwing> lowestMonth = swingRepository.findMonthWithLowestUtilization(headerId);
        Optional<OdSwing> widestSwingMonth = swingRepository.findMonthWithWidestSwing(headerId);

        long monthsExceedingThreshold = swingRepository.countMonthsExceedingThreshold(headerId, DEFAULT_THRESHOLD);

        Optional<OdSwing> latest = swingRepository.findLatestByHeaderId(headerId);

        Map<YearMonth, BigDecimal> utilizationTrend = swings.stream()
                .filter(s -> s.getMonth() != null && s.getUtilizationPercentage() != null)
                .collect(Collectors.toMap(
                        OdSwing::getMonth,
                        OdSwing::getUtilizationPercentage,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));

        Map<YearMonth, BigDecimal> swingRangeTrend = swings.stream()
                .filter(s -> s.getMonth() != null)
                .collect(Collectors.toMap(
                        OdSwing::getMonth,
                        s -> s.calculateSwingRange(),
                        (v1, v2) -> v1,
                        TreeMap::new
                ));

        return OdSwingSummaryDTO.builder()
                .headerId(headerId)
                .caseId(header.getCaseId())
                .accountHolder(header.getAccountHolder())
                .accountNumber(header.getAccountNumber())
                .sanctionLimit(header.getSanctionLimit())
                .averageHighestUtilization(avgHighest)
                .averageLowestUtilization(avgLowest)
                .averageUtilization(avgUtilization)
                .maxUtilization(maxUtilization)
                .minUtilization(minUtilization)
                .averageSwingRange(avgSwingRange)
                .maxSwingRange(maxSwingRange)
                .monthWithHighestUtilization(highestMonth.map(OdSwing::getMonth).orElse(null))
                .monthWithLowestUtilization(lowestMonth.map(OdSwing::getMonth).orElse(null))
                .monthWithWidestSwing(widestSwingMonth.map(OdSwing::getMonth).orElse(null))
                .totalMonths(swings.size())
                .monthsExceedingThreshold((int) monthsExceedingThreshold)
                .lastReportDate(latest.map(OdSwing::getDateHigh).orElse(null))
                .status(header.getStatus())
                .monthlyUtilizationTrend(utilizationTrend)
                .monthlySwingRangeTrend(swingRangeTrend)
                .build();
    }

    @Override
    public Map<String, Object> getHeaderStatistics(Long headerId) {
        log.info("Generating statistics for header ID: {}", headerId);

        OdSwingHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        BigDecimal avgUtilization = swingRepository.averageUtilizationByHeaderId(headerId);
        BigDecimal maxUtilization = swingRepository.maxUtilizationByHeaderId(headerId);
        BigDecimal minUtilization = swingRepository.minUtilizationByHeaderId(headerId);
        BigDecimal avgSwingRange = swingRepository.averageSwingRangeByHeaderId(headerId);
        BigDecimal maxSwingRange = swingRepository.maxSwingRangeByHeaderId(headerId);
        long recordCount = swingRepository.countByHeaderId(headerId);
        long monthsExceedingThreshold = swingRepository.countMonthsExceedingThreshold(headerId, DEFAULT_THRESHOLD);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("headerId", headerId);
        statistics.put("caseId", header.getCaseId());
        statistics.put("accountHolder", header.getAccountHolder());
        statistics.put("averageUtilization", avgUtilization);
        statistics.put("maxUtilization", maxUtilization);
        statistics.put("minUtilization", minUtilization);
        statistics.put("averageSwingRange", avgSwingRange);
        statistics.put("maxSwingRange", maxSwingRange);
        statistics.put("totalMonths", recordCount);
        statistics.put("monthsExceeding80Percent", monthsExceedingThreshold);
        statistics.put("sanctionLimit", header.getSanctionLimit());
        statistics.put("utilizationVsLimit", calculateUtilizationVsLimit(maxUtilization, header.getSanctionLimit()));

        return statistics;
    }

    @Override
    public List<OdSwingSummaryDTO> getAllHeadersSummary() {
        log.info("Generating summary for all headers");

        return headerRepository.findAll().stream()
                .map(header -> getHeaderSummary(header.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateAverageUtilization(Long headerId) {
        return swingRepository.averageUtilizationByHeaderId(headerId);
    }

    @Override
    public BigDecimal calculateMaxUtilization(Long headerId) {
        return swingRepository.maxUtilizationByHeaderId(headerId);
    }

    @Override
    public BigDecimal calculateMinUtilization(Long headerId) {
        return swingRepository.minUtilizationByHeaderId(headerId);
    }

    @Override
    public BigDecimal calculateAverageSwingRange(Long headerId) {
        return swingRepository.averageSwingRangeByHeaderId(headerId);
    }

    @Override
    public Map<YearMonth, BigDecimal> getUtilizationTrend(Long headerId) {
        log.info("Generating utilization trend for header ID: {}", headerId);

        List<OdSwing> swings = swingRepository.findByHeaderIdOrderByMonth(headerId);

        return swings.stream()
                .filter(s -> s.getMonth() != null && s.getUtilizationPercentage() != null)
                .collect(Collectors.toMap(
                        OdSwing::getMonth,
                        OdSwing::getUtilizationPercentage,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));
    }

    @Override
    public Map<YearMonth, BigDecimal> getSwingRangeTrend(Long headerId) {
        log.info("Generating swing range trend for header ID: {}", headerId);

        List<OdSwing> swings = swingRepository.findByHeaderIdOrderByMonth(headerId);

        return swings.stream()
                .filter(s -> s.getMonth() != null)
                .collect(Collectors.toMap(
                        OdSwing::getMonth,
                        OdSwing::calculateSwingRange,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));
    }

    @Override
    public List<OdSwingDTO> getMonthsExceedingThreshold(Long headerId, BigDecimal threshold) {
        log.info("Fetching months exceeding threshold {} for header ID: {}", threshold, headerId);

        return swingRepository.findByHeaderId(headerId).stream()
                .filter(s -> s.getUtilizationPercentage() != null &&
                        s.getUtilizationPercentage().compareTo(threshold) > 0)
                .map(this::mapSwingToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    private OdSwingHeader mapToEntity(OdSwingHeaderDTO dto) {
        OdSwingHeader header = new OdSwingHeader();
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

    private OdSwingHeaderDTO mapToDTO(OdSwingHeader header) {
        OdSwingHeaderDTO dto = OdSwingHeaderDTO.builder()
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

        // Map swing records if they exist
        if (header.getSwingRecords() != null && !header.getSwingRecords().isEmpty()) {
            List<OdSwingHeaderDTO.OdSwingDTO> swingDTOs =
                    header.getSwingRecords().stream()
                            .map(this::mapSwingToNestedDTO)
                            .collect(Collectors.toList());
            dto.setSwingRecords(swingDTOs);
        }

        return dto;
    }

    private OdSwingHeaderDTO.OdSwingDTO mapSwingToNestedDTO(OdSwing swing) {
        return OdSwingHeaderDTO.OdSwingDTO.builder()
                .id(swing.getId())
                .caseId(swing.getHeader().getCaseId())
                .month(swing.getMonth())
                .highestUtilization(swing.getHighestUtilization())
                .dateHigh(swing.getDateHigh())
                .lowestUtilization(swing.getLowestUtilization())
                .dateLow(swing.getDateLow())
                .utilizationPercentage(swing.getUtilizationPercentage())
                .build();
    }

    private OdSwingDTO mapSwingToDTO(OdSwing swing) {
        return OdSwingDTO.builder()
                .id(swing.getId())
                .month(swing.getMonth())
                .highestUtilization(swing.getHighestUtilization())
                .dateHigh(swing.getDateHigh())
                .lowestUtilization(swing.getLowestUtilization())
                .dateLow(swing.getDateLow())
                .utilizationPercentage(swing.getUtilizationPercentage())
                .headerId(swing.getHeader() != null ? swing.getHeader().getId() : null)
                .build();
    }

    private void updateHeaderFields(OdSwingHeader existing, OdSwingHeaderDTO dto) {
        if (dto.getAccountHolder() != null) existing.setAccountHolder(dto.getAccountHolder());
        if (dto.getAccountNumber() != null) existing.setAccountNumber(dto.getAccountNumber());
        if (dto.getIndustry() != null) existing.setIndustry(dto.getIndustry());
        if (dto.getSanctionLimit() != null) existing.setSanctionLimit(dto.getSanctionLimit());
        if (dto.getApprovedDate() != null) existing.setApprovedDate(dto.getApprovedDate());
        if (dto.getFacilityType() != null) existing.setFacilityType(dto.getFacilityType());
        if (dto.getReportDate() != null) existing.setReportDate(dto.getReportDate());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
    }

    private void validateHeader(OdSwingHeaderDTO dto) {
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

    private void validateSwing(OdSwingDTO dto) {
        if (dto.getHighestUtilization() != null && dto.getHighestUtilization().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Highest utilization cannot be negative");
        }
        if (dto.getLowestUtilization() != null && dto.getLowestUtilization().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Lowest utilization cannot be negative");
        }
        if (dto.getUtilizationPercentage() != null) {
            if (dto.getUtilizationPercentage().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Utilization percentage cannot be negative");
            }
            if (dto.getUtilizationPercentage().compareTo(new BigDecimal("100")) > 0) {
                throw new BadRequestException("Utilization percentage cannot exceed 100");
            }
        }
    }

    private String generateCaseId(OdSwingHeaderDTO dto) {
        String prefix = dto.getAccountHolder().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (prefix.length() > 5) {
            prefix = prefix.substring(0, 5);
        }
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        return "SWING-" + prefix + "-" + timestamp;
    }

    private BigDecimal calculateUtilizationVsLimit(BigDecimal utilization, BigDecimal sanctionLimit) {
        if (sanctionLimit == null || sanctionLimit.compareTo(BigDecimal.ZERO) == 0 || utilization == null) {
            return BigDecimal.ZERO;
        }
        return utilization.multiply(BigDecimal.valueOf(100))
                .divide(sanctionLimit, 2, RoundingMode.HALF_UP);
    }

    private OdSwingSummaryDTO buildEmptySummary(OdSwingHeader header) {
        return OdSwingSummaryDTO.builder()
                .headerId(header.getId())
                .caseId(header.getCaseId())
                .accountHolder(header.getAccountHolder())
                .accountNumber(header.getAccountNumber())
                .sanctionLimit(header.getSanctionLimit())
                .averageHighestUtilization(BigDecimal.ZERO)
                .averageLowestUtilization(BigDecimal.ZERO)
                .averageUtilization(BigDecimal.ZERO)
                .maxUtilization(BigDecimal.ZERO)
                .minUtilization(BigDecimal.ZERO)
                .averageSwingRange(BigDecimal.ZERO)
                .maxSwingRange(BigDecimal.ZERO)
                .monthWithHighestUtilization(null)
                .monthWithLowestUtilization(null)
                .monthWithWidestSwing(null)
                .totalMonths(0)
                .monthsExceedingThreshold(0)
                .lastReportDate(null)
                .status(header.getStatus())
                .monthlyUtilizationTrend(new TreeMap<>())
                .monthlySwingRangeTrend(new TreeMap<>())
                .build();
    }
}