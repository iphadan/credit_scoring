package com.cbo.credit_scoring.services.impls;

import com.cbo.credit_scoring.dtos.PreShipmentTurnoverHeaderDTO;
import com.cbo.credit_scoring.dtos.PreShipmentTurnoverDTO;
import com.cbo.credit_scoring.models.OdTurnoverHeader;
import com.cbo.credit_scoring.models.PreShipmentTurnoverHeader;
import com.cbo.credit_scoring.models.PreShipmentTurnover;
import com.cbo.credit_scoring.repositories.PreShipmentTurnoverHeaderRepository;
import com.cbo.credit_scoring.repositories.PreShipmentTurnoverRepository;
import com.cbo.credit_scoring.services.PreShipmentTurnoverHeaderService;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PreShipmentTurnoverHeaderServiceImpl implements PreShipmentTurnoverHeaderService {

    private final PreShipmentTurnoverHeaderRepository headerRepository;
    private final PreShipmentTurnoverRepository turnoverRepository;

    // In PreShipmentTurnoverHeaderServiceImpl.java

    @Override
    public List<PreShipmentTurnoverHeaderDTO> getHeadersByCaseId(String caseId) {
        log.info("Fetching pre-shipment headers for caseId: {}", caseId);

        Optional<PreShipmentTurnoverHeader> headers = headerRepository.findByCaseId(caseId);

        return headers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByCaseId(String caseId) {
//        log.info("Deleting all pre-shipment data for caseId: {}", caseId);
//
//        Optional<PreShipmentTurnoverHeader> headers = headerRepository.findByCaseId(caseId);
//        headerRepository.deleteAll(headers);
//
//        log.info("Deleted {} pre-shipment records for caseId: {}", headers.size(), caseId);
    }

    @Override
    public List<String> getAllCaseIds() {
        log.info("Fetching all unique caseIds from pre-shipment module");

        return headerRepository.findAllCaseIds();
    }

    @Override
    @Transactional
    public PreShipmentTurnoverHeaderDTO createHeader(PreShipmentTurnoverHeaderDTO headerDTO) {
        log.info("Creating new pre-shipment turnover header for customer: {}", headerDTO.getCustomerName());

        // Validate required fields
        validateHeader(headerDTO);

        // Generate caseId if not provided
        if (headerDTO.getCaseId() == null || headerDTO.getCaseId().trim().isEmpty()) {
            headerDTO.setCaseId(generateCaseId());
        }

        // Check for duplicate caseId
        if (headerRepository.existsByCaseId(headerDTO.getCaseId())) {
            throw new DuplicateResourceException("Header with caseId " + headerDTO.getCaseId() + " already exists");
        }

        // Check for duplicate account number if provided
        if (headerDTO.getAccountNumber() != null && !headerDTO.getAccountNumber().trim().isEmpty()) {
            if (headerRepository.existsByAccountNumber(headerDTO.getAccountNumber())) {
                throw new DuplicateResourceException("Header with account number " + headerDTO.getAccountNumber() + " already exists");
            }
        }

        // Convert DTO to Entity (header only)
        PreShipmentTurnoverHeader header = mapToEntity(headerDTO);

        // ===== IMPORTANT: Handle PreShipmentTurnover Records =====
        if (headerDTO.getTurnoverRecords() != null && !headerDTO.getTurnoverRecords().isEmpty()) {
            log.info("Processing {} pre-shipment turnover records", headerDTO.getTurnoverRecords().size());

            // Validate all turnover records first
            validateTurnoverRecords(headerDTO.getTurnoverRecords());

            // Check for duplicate months within the request
            checkForDuplicateMonths(headerDTO.getTurnoverRecords());

            // Create and add each turnover record to the header
            for (PreShipmentTurnoverHeaderDTO.PreShipmentTurnoverDTO turnoverDTO : headerDTO.getTurnoverRecords()) {

                // Create turnover entity
                PreShipmentTurnover turnover = new PreShipmentTurnover();
                turnover.setMonth(turnoverDTO.getMonth());
//                turnover.setLcNumber(turnoverDTO.getLcNumber());
//                turnover.setInvoiceNumber(turnoverDTO.getInvoiceNumber());
//                turnover.setContractNumber(turnoverDTO.getContractNumber());
                turnover.setDebitDisbursements(turnoverDTO.getDebitDisbursements() != null ?
                        turnoverDTO.getDebitDisbursements() : BigDecimal.ZERO);
                turnover.setCreditPrincipalRepayments(turnoverDTO.getCreditPrincipalRepayments() != null ?
                        turnoverDTO.getCreditPrincipalRepayments() : BigDecimal.ZERO);

                // Use the helper method to establish bidirectional relationship
                    header.addTurnoverRecord(turnover);

                log.debug("Added turnover record for month: {}", turnoverDTO.getMonth());
            }
        }

        // Save header - THIS WILL ALSO SAVE ALL TURNOVER RECORDS due to CascadeType.ALL
        PreShipmentTurnoverHeader savedHeader = headerRepository.save(header);
        log.info("Header created successfully with ID: {}, CaseId: {}, with {} turnover records",
                savedHeader.getId(), savedHeader.getCaseId(),
                savedHeader.getTurnoverRecords() != null ? savedHeader.getTurnoverRecords().size() : 0);

        return mapToDTO(savedHeader);
    }

    /**
     * Validate all turnover records
     */
    private void validateTurnoverRecords(List<PreShipmentTurnoverHeaderDTO.PreShipmentTurnoverDTO> turnoverRecords) {
        for (PreShipmentTurnoverHeaderDTO.PreShipmentTurnoverDTO dto : turnoverRecords) {
            if (dto.getMonth() == null) {
                throw new BadRequestException("Month is required for each turnover record");
            }
            if (dto.getDebitDisbursements() != null && dto.getDebitDisbursements().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Debit disbursements cannot be negative for month: " + dto.getMonth());
            }
            if (dto.getCreditPrincipalRepayments() != null && dto.getCreditPrincipalRepayments().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Credit principal repayments cannot be negative for month: " + dto.getMonth());
            }
        }
    }

    /**
     * Check for duplicate months within the same request
     */
    private void checkForDuplicateMonths(List<PreShipmentTurnoverHeaderDTO.PreShipmentTurnoverDTO> turnoverRecords) {
        Set<YearMonth> months = new HashSet<>();
        for (PreShipmentTurnoverHeaderDTO.PreShipmentTurnoverDTO dto : turnoverRecords) {
            if (!months.add(dto.getMonth())) {
                throw new BadRequestException("Duplicate month " + dto.getMonth() + " found in request");
            }
        }
    }

    @Override
    public PreShipmentTurnoverHeaderDTO updateHeader(Long id, PreShipmentTurnoverHeaderDTO headerDTO) {
        log.info("Updating pre-shipment turnover header with ID: {}", id);

        // Find existing header
        PreShipmentTurnoverHeader existingHeader = headerRepository.findById(id)
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
        PreShipmentTurnoverHeader updatedHeader = headerRepository.save(existingHeader);
        log.info("Header updated successfully with ID: {}", updatedHeader.getId());

        return mapToDTO(updatedHeader);
    }

    @Override
    public PreShipmentTurnoverHeaderDTO getHeaderById(Long id) {
        log.info("Fetching pre-shipment turnover header with ID: {}", id);

        PreShipmentTurnoverHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + id));

        return mapToDTO(header);
    }

    @Override
    public PreShipmentTurnoverHeaderDTO getHeaderByCaseId(String caseId) {
        log.info("Fetching pre-shipment turnover header with CaseId: {}", caseId);

        PreShipmentTurnoverHeader header = headerRepository.findByCaseId(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with CaseId: " + caseId));

        return mapToDTO(header);
    }

    @Override
    public Page<PreShipmentTurnoverHeaderDTO> getAllHeaders(Pageable pageable) {
        log.info("Fetching all pre-shipment turnover headers with pagination");

        return headerRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public void deleteHeader(Long id) {
        log.info("Deleting pre-shipment turnover header with ID: {}", id);

        PreShipmentTurnoverHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + id));

        // The cascade will automatically delete all turnover records
        headerRepository.delete(header);
        log.info("Header deleted successfully with ID: {}", id);
    }

    @Override
    public List<PreShipmentTurnoverHeaderDTO> searchByCustomerName(String customerName) {
        log.info("Searching headers by customer name: {}", customerName);

        return headerRepository.findByCustomerNameContainingIgnoreCase(customerName)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PreShipmentTurnoverHeaderDTO getHeaderByAccountNumber(String accountNumber) {
        log.info("Fetching header by account number: {}", accountNumber);

        PreShipmentTurnoverHeader header = headerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with account number: " + accountNumber));

        return mapToDTO(header);
    }

    @Override
    public List<PreShipmentTurnoverHeaderDTO> getHeadersByTypeOfFacility(String typeOfFacility) {
        log.info("Fetching headers by type of facility: {}", typeOfFacility);

        return headerRepository.findByTypeOfFacility(typeOfFacility)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreShipmentTurnoverHeaderDTO> getHeadersByIndustryType(String industryType) {
        log.info("Fetching headers by industry type: {}", industryType);

        return headerRepository.findByIndustryType(industryType)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreShipmentTurnoverHeaderDTO> getHeadersByDateApprovedRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching headers by date approved between: {} and {}", startDate, endDate);

        return headerRepository.findByDateApprovedBetween(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreShipmentTurnoverHeaderDTO> getHeadersByReportingDate(LocalDate reportingDate) {
        log.info("Fetching headers by reporting date: {}", reportingDate);

        return headerRepository.findByReportingDate(reportingDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreShipmentTurnoverHeaderDTO> searchHeaders(String searchTerm) {
        log.info("Searching headers by any field with term: {}", searchTerm);

        return headerRepository.searchByAnyField(searchTerm)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PreShipmentTurnoverHeaderDTO addTurnoverRecord(Long headerId, PreShipmentTurnoverDTO turnoverDTO) {
        log.info("Adding turnover record to header ID: {}", headerId);

        PreShipmentTurnoverHeader header = headerRepository.findById(headerId)
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
        PreShipmentTurnover turnover = new PreShipmentTurnover();
        turnover.setMonth(turnoverDTO.getMonth());
        turnover.setDebitDisbursements(turnoverDTO.getDebitDisbursements() != null ?
                turnoverDTO.getDebitDisbursements() : BigDecimal.ZERO);
        turnover.setCreditPrincipalRepayments(turnoverDTO.getCreditPrincipalRepayments() != null ?
                turnoverDTO.getCreditPrincipalRepayments() : BigDecimal.ZERO);

        // Add to header using helper method
        header.addTurnoverRecord(turnover);

        // Save header (cascade will save turnover)
        PreShipmentTurnoverHeader updatedHeader = headerRepository.save(header);
        log.info("Turnover record added successfully to header ID: {}", headerId);

        return mapToDTO(updatedHeader);
    }

    @Override
    @Transactional
    public PreShipmentTurnoverHeaderDTO updateTurnoverRecord(Long headerId, Long turnoverId, PreShipmentTurnoverDTO turnoverDTO) {
        log.info("Updating turnover record ID: {} in header ID: {}", turnoverId, headerId);

        PreShipmentTurnoverHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        PreShipmentTurnover turnover = turnoverRepository.findById(turnoverId)
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
        if (turnoverDTO.getDebitDisbursements() != null) turnover.setDebitDisbursements(turnoverDTO.getDebitDisbursements());
        if (turnoverDTO.getCreditPrincipalRepayments() != null) turnover.setCreditPrincipalRepayments(turnoverDTO.getCreditPrincipalRepayments());

        // Save turnover
        turnoverRepository.save(turnover);
        log.info("Turnover record updated successfully");

        return mapToDTO(header);
    }

    @Override
    @Transactional
    public PreShipmentTurnoverHeaderDTO removeTurnoverRecord(Long headerId, Long turnoverId) {
        log.info("Removing turnover record ID: {} from header ID: {}", turnoverId, headerId);

        PreShipmentTurnoverHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        PreShipmentTurnover turnover = turnoverRepository.findById(turnoverId)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + turnoverId));

        // Verify turnover belongs to header
        if (!turnover.getHeader().getId().equals(headerId)) {
            throw new BadRequestException("Turnover record does not belong to this header");
        }

        // Remove using helper method
        header.removeTurnoverRecord(turnover);

        // Save header (cascade will remove turnover)
        PreShipmentTurnoverHeader updatedHeader = headerRepository.save(header);
        log.info("Turnover record removed successfully from header ID: {}", headerId);

        return mapToDTO(updatedHeader);
    }

    @Override
    public Map<String, Object> getHeaderStatistics(Long headerId) {
        log.info("Generating statistics for header ID: {}", headerId);

        PreShipmentTurnoverHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        BigDecimal totalDebit = turnoverRepository.sumDebitByHeaderId(headerId);
        BigDecimal totalCredit = turnoverRepository.sumCreditByHeaderId(headerId);
        BigDecimal netTurnover = totalDebit.subtract(totalCredit);
        long recordCount = turnoverRepository.countByHeaderId(headerId);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("headerId", headerId);
        statistics.put("caseId", header.getCaseId());
        statistics.put("customerName", header.getCustomerName());
        statistics.put("totalDebit", totalDebit);
        statistics.put("totalCredit", totalCredit);
        statistics.put("netTurnover", netTurnover);
        statistics.put("recordCount", recordCount);
        statistics.put("approvedAmount", header.getApprovedAmount());
        statistics.put("utilizationPercentage", calculateUtilizationPercentage(netTurnover, header.getApprovedAmount()));

        return statistics;
    }

    @Override
    public BigDecimal calculateTotalDebit(Long headerId) {
        return turnoverRepository.sumDebitByHeaderId(headerId);
    }

    @Override
    public BigDecimal calculateTotalCredit(Long headerId) {
        return turnoverRepository.sumCreditByHeaderId(headerId);
    }

    @Override
    public BigDecimal calculateNetTurnover(Long headerId) {
        return turnoverRepository.calculateNetTurnoverByHeaderId(headerId);
    }

    @Override
    public List<Map<String, Object>> getMonthlyStatistics(Long headerId) {
        log.info("Generating monthly statistics for header ID: {}", headerId);

        List<Object[]> monthlyData = turnoverRepository.getMonthlySummaryByHeaderId(headerId);

        List<Map<String, Object>> monthlyStats = new ArrayList<>();
        for (Object[] data : monthlyData) {
            Map<String, Object> monthStat = new HashMap<>();
            monthStat.put("month", data[0]);
            monthStat.put("totalDebit", data[1]);
            monthStat.put("totalCredit", data[2]);
            monthStat.put("netTurnover", ((BigDecimal) data[1]).subtract((BigDecimal) data[2]));
            monthlyStats.add(monthStat);
        }

        return monthlyStats;
    }

    // Helper methods for mapping and validation
    private PreShipmentTurnoverHeader mapToEntity(PreShipmentTurnoverHeaderDTO dto) {
        PreShipmentTurnoverHeader header = new PreShipmentTurnoverHeader();
        header.setCaseId(dto.getCaseId());
        header.setCustomerName(dto.getCustomerName());
        header.setTypeOfFacility(dto.getTypeOfFacility());
        header.setIndustryType(dto.getIndustryType());
        header.setAccountNumber(dto.getAccountNumber());
        header.setApprovedAmount(dto.getApprovedAmount());
        header.setDateApproved(dto.getDateApproved());
        header.setReportingDate(dto.getReportingDate());
        return header;
    }

    private PreShipmentTurnoverHeaderDTO mapToDTO(PreShipmentTurnoverHeader header) {
        System.out.println(header);
        PreShipmentTurnoverHeaderDTO dto = PreShipmentTurnoverHeaderDTO.builder()
                .id(header.getId())
                .caseId(header.getCaseId())
                .customerName(header.getCustomerName())
                .typeOfFacility(header.getTypeOfFacility())
                .industryType(header.getIndustryType())
                .accountNumber(header.getAccountNumber())
                .approvedAmount(header.getApprovedAmount())
                .dateApproved(header.getDateApproved())
                .reportingDate(header.getReportingDate())
                .caseId(header.getCaseId())
                .build();

        // Map turnover records if they exist
        if (header.getTurnoverRecords() != null && !header.getTurnoverRecords().isEmpty()) {
            List<PreShipmentTurnoverHeaderDTO.PreShipmentTurnoverDTO> turnoverDTOs =
                    header.getTurnoverRecords().stream()
                            .map(this::mapTurnoverToNestedDTO)
                            .collect(Collectors.toList());
            dto.setTurnoverRecords(turnoverDTOs);
        }

        return dto;
    }

    private PreShipmentTurnoverHeaderDTO.PreShipmentTurnoverDTO mapTurnoverToNestedDTO(PreShipmentTurnover turnover) {
        PreShipmentTurnoverHeaderDTO.PreShipmentTurnoverDTO dto =
                new PreShipmentTurnoverHeaderDTO.PreShipmentTurnoverDTO();
        dto.setId(turnover.getId());
        dto.setCaseId(turnover.getHeader().getCaseId()); // Get caseId from header
        dto.setMonth(turnover.getMonth());
        dto.setDebitDisbursements(turnover.getDebitDisbursements());
        dto.setCreditPrincipalRepayments(turnover.getCreditPrincipalRepayments());
        return dto;
    }

    private void updateHeaderFields(PreShipmentTurnoverHeader existing, PreShipmentTurnoverHeaderDTO dto) {
        if (dto.getCustomerName() != null) existing.setCustomerName(dto.getCustomerName());
        if (dto.getTypeOfFacility() != null) existing.setTypeOfFacility(dto.getTypeOfFacility());
        if (dto.getIndustryType() != null) existing.setIndustryType(dto.getIndustryType());
        if (dto.getAccountNumber() != null) existing.setAccountNumber(dto.getAccountNumber());
        if (dto.getApprovedAmount() != null) existing.setApprovedAmount(dto.getApprovedAmount());
        if (dto.getDateApproved() != null) existing.setDateApproved(dto.getDateApproved());
        if (dto.getReportingDate() != null) existing.setReportingDate(dto.getReportingDate());
    }

    private void validateHeader(PreShipmentTurnoverHeaderDTO dto) {
        if (dto.getCustomerName() == null || dto.getCustomerName().trim().isEmpty()) {
            throw new BadRequestException("Customer name is required");
        }
        if (dto.getApprovedAmount() != null && dto.getApprovedAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Approved amount cannot be negative");
        }
    }

    private void validateTurnover(PreShipmentTurnoverDTO dto) {
        if (dto.getDebitDisbursements() != null && dto.getDebitDisbursements().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Debit disbursements cannot be negative");
        }
        if (dto.getCreditPrincipalRepayments() != null && dto.getCreditPrincipalRepayments().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Credit principal repayments cannot be negative");
        }
    }

    private String generateCaseId() {
        // Get current timestamp in milliseconds
        long milliseconds = System.currentTimeMillis();

        // Get current date in YYMMDD format
        LocalDate today = LocalDate.now();
        String datePart = today.format(DateTimeFormatter.ofPattern("yyMMdd"));

        // Add header ID at the end for extra uniqueness (optional)
        return String.format("cbocrs%d-%s", milliseconds, datePart);
    }


    private BigDecimal calculateUtilizationPercentage(BigDecimal netTurnover, BigDecimal approvedAmount) {
        if (approvedAmount == null || approvedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return netTurnover.multiply(BigDecimal.valueOf(100))
                .divide(approvedAmount, 2, BigDecimal.ROUND_HALF_UP);
    }
}